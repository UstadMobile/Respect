package world.respect.datalayer.db.school.xapi.adapters

import androidx.room.Embedded
import androidx.room.Relation
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
import world.respect.datalayer.db.school.xapi.ext.langMapPropEnum
import world.respect.datalayer.db.school.xapi.ext.toLangMap
import world.respect.datalayer.school.xapi.model.XapiActivity
import world.respect.libutil.ext.toEmptyIfNull
import kotlin.collections.component1
import kotlin.collections.component2


/**
 * @param statementContextActivityJoin Join entity used where the activity is part of a Statement's
 * contextActivities
 */
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

    val statementContextActivityJoin: StatementContextActivityJoin? = null,
)


fun XapiActivity?.toEntities(
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

    fun XapiActivity.Interaction.toEntities(
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
            actInteractionType = this?.interactionType?.dbFlag ?: ActivityEntity.TYPE_UNSET,
            actCorrectResponsePatterns = this?.correctResponsesPattern?.let { json.encodeToString(it) },
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

fun ActivityEntities.toModel(
    json: Json
) : XapiActivity {

    fun interactionsForProp(
        interactionProp: ActivityInteractionEntityPropEnum,
    ): List<XapiActivity.Interaction> = activityInteractionEntities.filter {
        it.aieProp == interactionProp
    }.map { interactionEntity ->
        XapiActivity.Interaction(
            id = interactionEntity.aieId,
            description = activityLangMapEntries.toLangMap {
                it.almeProperty == interactionEntity.aieProp.langMapPropEnum &&
                        it.almeInteractionId == interactionEntity.aieId
            }
        )
    }

    return XapiActivity(
        name = activityLangMapEntries.toLangMap {
            it.almeProperty == ActivityLangMapEntryPropEnum.NAME
        },
        description = activityLangMapEntries.toLangMap {
            it.almeProperty == ActivityLangMapEntryPropEnum.DESCRIPTION
        },
        type = activityEntity.actType,
        extensions = activityExtensionEntities.associate {
            it.aeeKey to json.decodeFromString(
                JsonElement.serializer(), it.aeeJson
            )
        },
        moreInfo = activityEntity.actMoreInfo,
        //TODO: interaction type - should be enum
        choices = interactionsForProp(ActivityInteractionEntityPropEnum.CHOICES),
        scale = interactionsForProp(ActivityInteractionEntityPropEnum.SCALE),
        source = interactionsForProp(ActivityInteractionEntityPropEnum.SOURCE),
        target = interactionsForProp(ActivityInteractionEntityPropEnum.TARGET),
        steps = interactionsForProp(ActivityInteractionEntityPropEnum.STEPS),

    )
}