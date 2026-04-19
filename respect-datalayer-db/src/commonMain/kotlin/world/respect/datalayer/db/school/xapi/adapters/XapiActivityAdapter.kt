package world.respect.datalayer.db.school.xapi.adapters

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.xapi.entities.XapiActivityEntity
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
import world.respect.datalayer.school.xapi.model.XapiObjectType
import world.respect.libutil.ext.toEmptyIfNull
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Instant


data class XapiActivityEntities(
    @Embedded
    val activityEntity: XapiActivityEntity,

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


fun XapiActivity.toEntities(
    uidNumberMapper: UidNumberMapper,
    json: Json,
    lastModified: Instant,
): XapiActivityEntities? {
    val actDefinition = definition ?: return null

    val activityUid = uidNumberMapper(id)

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
            almeLastModified = lastModified,
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
        actDefinition.choices?.map {
            it.toEntities(ActivityInteractionEntityPropEnum.CHOICES)
        }?.also { addAll(it) }

        actDefinition.scale?.map {
            it.toEntities(ActivityInteractionEntityPropEnum.SCALE)
        }?.also { addAll(it) }

        actDefinition.source?.map {
            it.toEntities(ActivityInteractionEntityPropEnum.SOURCE)
        }?.also { addAll(it) }


        actDefinition.target?.map {
            it.toEntities(ActivityInteractionEntityPropEnum.TARGET)
        }?.also { addAll(it) }

        actDefinition.steps?.map {
            it.toEntities(ActivityInteractionEntityPropEnum.STEPS)
        }?.also { addAll(it) }
    }

    return XapiActivityEntities(
        activityEntity = XapiActivityEntity(
            actUid = activityUid,
            actIdIri = id,
            actObjectTypeSet = objectType != null,
            actType = actDefinition.type,
            actMoreInfo = actDefinition.moreInfo,
            actInteractionType = actDefinition.interactionType,
            actCorrectResponsePatterns = actDefinition.correctResponsesPattern?.let {
                json.encodeToString(it)
            },
            actFlags = flagsOf(
                XapiActivityEntity.FLAG_EXTENSIONS_NULL to (actDefinition.extensions == null),
                XapiActivityEntity.FLAG_CHOICES_NULL to (actDefinition.choices == null),
                XapiActivityEntity.FLAG_SCALE_NULL to (actDefinition.scale == null),
                XapiActivityEntity.FLAG_SOURCE_NULL to (actDefinition.source == null),
                XapiActivityEntity.FLAG_TARGET_NULL to (actDefinition.target == null),
                XapiActivityEntity.FLAG_STEPS_NULL to (actDefinition.steps == null),
            ),
            actSignificantLastModified = lastModified,
            actNonSignificantLastModified = lastModified,
        ),
        activityLangMapEntries = buildList {
            actDefinition.name?.toLangMapEntries(
                property = ActivityLangMapEntryPropEnum.NAME, interactionId = null
            )?.also { addAll(it) }

            actDefinition.description?.toLangMapEntries(
                property = ActivityLangMapEntryPropEnum.DESCRIPTION, interactionId = null
            )?.also { addAll(it) }

            addAll(interactionEntitiesAndLangMaps.flatMap { it.second })
        },
        activityInteractionEntities = interactionEntitiesAndLangMaps.map { it.first },
        activityExtensionEntities = actDefinition.extensions?.map { (key, value) ->
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
fun XapiActivityEntities.toModel(
    json: Json
) : XapiActivity {

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

    return XapiActivity(
        id = this.activityEntity.actIdIri,
        objectType = XapiObjectType.Activity.takeIf { this.activityEntity.actObjectTypeSet },
        definition = XapiActivityDefinition(
            name = activityLangMapEntries.toLangMap {
                it.almeProperty == ActivityLangMapEntryPropEnum.NAME
            }.takeIfNotEmpty(),
            description = activityLangMapEntries.toLangMap {
                it.almeProperty == ActivityLangMapEntryPropEnum.DESCRIPTION
            }.takeIfNotEmpty(),
            type = activityEntity.actType,
            extensions = activityExtensionEntities.takeIf {
                !activityEntity.actFlags.hasFlag(XapiActivityEntity.FLAG_EXTENSIONS_NULL)
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

