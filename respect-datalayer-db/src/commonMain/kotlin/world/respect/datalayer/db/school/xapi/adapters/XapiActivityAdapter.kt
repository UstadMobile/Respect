package world.respect.datalayer.db.school.xapi.adapters

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.xapi.entities.ActivityEntity
import world.respect.datalayer.db.school.xapi.entities.ActivityExtensionEntity
import world.respect.datalayer.db.school.xapi.entities.ActivityInteractionEntity
import world.respect.datalayer.db.school.xapi.entities.ActivityInteractionEntityPropEnum
import world.respect.datalayer.db.school.xapi.entities.ActivityLangMapEntry
import world.respect.datalayer.db.school.xapi.entities.ActivityLangMapEntryPropEnum
import world.respect.datalayer.db.school.xapi.entities.StatementContextActivityJoin
import world.respect.datalayer.db.school.xapi.entities.StatementContextActivityJoinTypeEnum
import world.respect.datalayer.db.school.xapi.ext.langMapPropEnum
import world.respect.datalayer.db.school.xapi.ext.toLangMap
import world.respect.datalayer.school.xapi.ext.flagsOf
import world.respect.datalayer.school.xapi.ext.hasFlag
import world.respect.datalayer.school.xapi.ext.takeIfNotEmpty
import world.respect.datalayer.school.xapi.model.XapiActivity
import world.respect.datalayer.school.xapi.model.XapiActivityDefinition
import world.respect.libutil.ext.toEmptyIfNull
import kotlin.collections.component1
import kotlin.collections.component2


data class ActivityEntities(
    @Embedded
    val activityEntity: ActivityEntity,

    @Relation(
        parentColumn = "actUid",
        entityColumn = "almeActivityUid"
    )
    val activityLangMapEntries: List<ActivityLangMapEntry> = emptyList(),

    @Relation(
        parentColumn = "actUid",
        entityColumn = "aieActivityUid"
    )
    val activityInteractionEntities: List<ActivityInteractionEntity>  = emptyList(),

    @Relation(
        parentColumn = "actUid",
        entityColumn = "aeeActivityUid"
    )
    val activityExtensionEntities: List<ActivityExtensionEntity> = emptyList(),
)


fun XapiActivityDefinition?.toEntities(
    activityId: String,
    uidNumberMapper: UidNumberMapper,
    json: Json,
): ActivityEntities {
    val activityUid = uidNumberMapper(activityId)

    fun Map<String, String>.toLangMapEntries(
        property: ActivityLangMapEntryPropEnum,
        interactionId: String?,
    ) = entries.map { (lang, text) ->
        ActivityLangMapEntry(
            almeActivityUid = activityUid,
            almeLangCode = lang,
            almeProperty = property,
            almeValue = text,
            almeInteractionId = interactionId,
        )
    }

    fun XapiActivityDefinition.Interaction.toEntities(
        interactionProp: ActivityInteractionEntityPropEnum,
    ): Pair<ActivityInteractionEntity, List<ActivityLangMapEntry>> {

        return ActivityInteractionEntity(
            aieActivityUid = activityUid,
            aieProp = interactionProp,
            aieId = id,
        ) to description?.toLangMapEntries(
            property = interactionProp.langMapPropEnum,
            interactionId = id,
        ).toEmptyIfNull()
    }

    val interactionEntitiesAndLangMaps = buildList {
        this@toEntities?.choices?.map {
            it.toEntities(ActivityInteractionEntityPropEnum.CHOICES)
        }?.also { addAll(it) }

        this@toEntities?.scale?.map {
            it.toEntities(ActivityInteractionEntityPropEnum.SCALE)
        }?.also { addAll(it) }

        this@toEntities?.source?.map {
            it.toEntities(ActivityInteractionEntityPropEnum.SOURCE)
        }?.also { addAll(it) }


        this@toEntities?.target?.map {
            it.toEntities(ActivityInteractionEntityPropEnum.TARGET)
        }?.also { addAll(it) }

        this@toEntities?.steps?.map {
            it.toEntities(ActivityInteractionEntityPropEnum.STEPS)
        }?.also { addAll(it) }
    }

    return ActivityEntities(
        activityEntity = ActivityEntity(
            actUid = activityUid,
            actIdIri = activityId,
            actType = this?.type,
            actMoreInfo = this?.moreInfo,
            actInteractionType = this?.interactionType,
            actCorrectResponsePatterns = this?.correctResponsesPattern?.let {
                json.encodeToString(it)
            },
            actFlags = flagsOf(
                ActivityEntity.FLAG_EXTENSIONS_NULL to (this?.extensions == null),
                ActivityEntity.FLAG_CHOICES_NULL to (this?.choices == null),
                ActivityEntity.FLAG_SCALE_NULL to (this?.scale == null),
                ActivityEntity.FLAG_SOURCE_NULL to (this?.source == null),
                ActivityEntity.FLAG_TARGET_NULL to (this?.target == null),
                ActivityEntity.FLAG_STEPS_NULL to (this?.steps == null),
            )
        ),
        activityLangMapEntries = buildList {
            this@toEntities?.name?.toLangMapEntries(
                property = ActivityLangMapEntryPropEnum.NAME, interactionId = null
            )?.also { addAll(it) }

            this@toEntities?.description?.toLangMapEntries(
                property = ActivityLangMapEntryPropEnum.DESCRIPTION, interactionId = null
            )?.also { addAll(it) }

            addAll(interactionEntitiesAndLangMaps.flatMap { it.second })
        },
        activityInteractionEntities = interactionEntitiesAndLangMaps.map { it.first },
        activityExtensionEntities = this?.extensions?.map { (key, value) ->
            ActivityExtensionEntity(
                aeeActivityUid = activityUid,
                aeeKeyHash = uidNumberMapper(key),
                aeeKey = key, //must be valid IRI,
                aeeJson = json.encodeToString(JsonElement.serializer(), value)
            )
        } ?: emptyList()
    )
}

/**
 * As per the xAPI spec: Statements and other objects SHOULD NOT include properties with a value of
 * an empty object.
 *
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#22-formatting-requirements
 */
fun ActivityEntities.toModel(
    json: Json
) : XapiActivityDefinition {

    fun interactionsForProp(
        interactionProp: ActivityInteractionEntityPropEnum,
    ): List<XapiActivityDefinition.Interaction>? = activityInteractionEntities.filter {
        it.aieProp == interactionProp
    }.map { interactionEntity ->
        XapiActivityDefinition.Interaction(
            id = interactionEntity.aieId,
            description = activityLangMapEntries.toLangMap {
                it.almeProperty == interactionEntity.aieProp.langMapPropEnum &&
                        it.almeInteractionId == interactionEntity.aieId
            }
        )
    }.takeIf {
        it.isNotEmpty() || !activityEntity.actFlags.hasFlag(interactionProp.listIsNullFlag)
    }

    return XapiActivityDefinition(
        name = activityLangMapEntries.toLangMap {
            it.almeProperty == ActivityLangMapEntryPropEnum.NAME
        }.takeIfNotEmpty(),
        description = activityLangMapEntries.toLangMap {
            it.almeProperty == ActivityLangMapEntryPropEnum.DESCRIPTION
        }.takeIfNotEmpty(),
        type = activityEntity.actType,
        extensions = activityExtensionEntities.takeIf {
           !activityEntity.actFlags.hasFlag(ActivityEntity.FLAG_EXTENSIONS_NULL)
        }?.associate {
            it.aeeKey to json.decodeFromString(
                JsonElement.serializer(), it.aeeJson
            )
        },
        moreInfo = activityEntity.actMoreInfo,
        interactionType = activityEntity.actInteractionType,
        correctResponsesPattern = activityEntity.actCorrectResponsePatterns?.let {
            json.decodeFromString(
                ListSerializer(String.serializer()), it
            )
        },
        choices = interactionsForProp(ActivityInteractionEntityPropEnum.CHOICES),
        scale = interactionsForProp(ActivityInteractionEntityPropEnum.SCALE),
        source = interactionsForProp(ActivityInteractionEntityPropEnum.SOURCE),
        target = interactionsForProp(ActivityInteractionEntityPropEnum.TARGET),
        steps = interactionsForProp(ActivityInteractionEntityPropEnum.STEPS),
    )
}

fun XapiActivity.toContextActivityJoinEntity(
    type: StatementContextActivityJoinTypeEnum,
    uidNumberMapper: UidNumberMapper,
    statementUuidHi: Long,
    statementUuidLo: Long,
) : StatementContextActivityJoin {
    return StatementContextActivityJoin(
        scajFromStatementIdHi = statementUuidHi,
        scajFromStatementIdLo = statementUuidLo,
        scajContextType = type,
        scajToActivityId = this.id,
        scajToActivityUid = uidNumberMapper(this.id)
    )
}
fun List<ActivityEntities>.flattenActivities(): List<ActivityEntities> {
    return distinctBy { it.activityEntity.actUid }.map { distinctActivity ->
        val allByUid = this.filter {
            it.activityEntity.actUid == distinctActivity.activityEntity.actUid
        }

        val interactions = allByUid.flatMap { it.activityInteractionEntities }

        ActivityEntities(
            activityEntity = ActivityEntity(
                actUid = distinctActivity.activityEntity.actUid,
                actIdIri = distinctActivity.activityEntity.actIdIri,
                actType = allByUid.firstNotNullOfOrNull { it.activityEntity.actType },
                actMoreInfo = allByUid.firstNotNullOfOrNull { it.activityEntity.actMoreInfo },
                actInteractionType = allByUid.firstNotNullOfOrNull { it.activityEntity.actInteractionType },
                actCorrectResponsePatterns = allByUid.firstNotNullOfOrNull {
                    it.activityEntity.actCorrectResponsePatterns
                },
                actFlags = allByUid.firstOrNull {
                    it.activityEntity.actFlags != 0
                }?.activityEntity?.actFlags ?: 0,
            ),
            activityLangMapEntries = allByUid.flatMap { it.activityLangMapEntries },
            activityExtensionEntities = allByUid.flatMap { it.activityExtensionEntities },
            activityInteractionEntities = interactions,
        )
    }
}
