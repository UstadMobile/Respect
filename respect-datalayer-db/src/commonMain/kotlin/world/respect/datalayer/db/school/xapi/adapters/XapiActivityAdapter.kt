package world.respect.datalayer.db.school.xapi.adapters

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.xapi.entities.ActivityEntity
import world.respect.datalayer.db.school.xapi.entities.ActivityExtensionEntity
import world.respect.datalayer.db.school.xapi.entities.ActivityInteractionEntity
import world.respect.datalayer.db.school.xapi.entities.ActivityInteractionEntity.Companion.PROP_CHOICES
import world.respect.datalayer.db.school.xapi.entities.ActivityInteractionEntity.Companion.PROP_SCALE
import world.respect.datalayer.db.school.xapi.entities.ActivityInteractionEntity.Companion.PROP_SOURCE
import world.respect.datalayer.db.school.xapi.entities.ActivityInteractionEntity.Companion.PROP_STEPS
import world.respect.datalayer.db.school.xapi.entities.ActivityInteractionEntity.Companion.PROP_TARGET
import world.respect.datalayer.db.school.xapi.entities.ActivityLangMapEntry
import world.respect.datalayer.db.school.xapi.entities.StatementContextActivityJoin
import world.respect.datalayer.school.xapi.model.XapiActivity
import world.respect.libutil.ext.toEmptyIfNull
import kotlin.collections.component1
import kotlin.collections.component2


/**
 * @param statementContextActivityJoin Join entity used where the activity is part of a Statement's
 * contextActivities
 */
data class ActivityEntities(
    val activityEntity: ActivityEntity,
    val activityLangMapEntries: List<ActivityLangMapEntry> = emptyList(),
    val activityInteractionEntities: List<ActivityInteractionEntity>  = emptyList(),
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
        propName: String,
        almeAieHash: Long = 0,
    ) = entries.map { (lang, text) ->
        ActivityLangMapEntry(
            almeActivityUid = activityUid,
            almeHash = uidNumberMapper("$propName-$lang"),
            almeLangCode = lang,
            almePropName = propName,
            almeValue = text,
            almeAieHash = almeAieHash,
        )
    }

    fun XapiActivity.Interaction.toEntities(
        propId: Int,
        propName: String,
    ): Pair<ActivityInteractionEntity, List<ActivityLangMapEntry>> {
        val aieHash = uidNumberMapper("$propId$id")

        return ActivityInteractionEntity(
            aieActivityUid = activityUid,
            aieHash = aieHash,
            aieProp = propId,
            aieId = id,
        ) to description?.toLangMapEntries(
            "$propName-$id", almeAieHash = aieHash
        ).toEmptyIfNull()
    }

    val interactionEntitiesAndLangMaps =
        this?.choices?.map { it.toEntities(PROP_CHOICES, "choices") }.toEmptyIfNull() +
        this?.scale?.map { it.toEntities(PROP_SCALE, "scale") }.toEmptyIfNull() +
        this?.source?.map { it.toEntities(PROP_SOURCE, "source") }.toEmptyIfNull() +
        this?.target?.map { it.toEntities(PROP_TARGET, "target") }.toEmptyIfNull() +
        this?.steps?.map { it.toEntities(PROP_STEPS, "steps") }.toEmptyIfNull()

    return ActivityEntities(
        activityEntity = ActivityEntity(
            actUid = activityUid,
            actIdIri = activityId,
            actType = this?.type,
            actMoreInfo = this?.moreInfo,
            actInteractionType = this?.interactionType?.dbFlag ?: ActivityEntity.TYPE_UNSET,
            actCorrectResponsePatterns = this?.correctResponsesPattern?.let { json.encodeToString(it) },
        ),
        activityLangMapEntries =
            this?.name?.toLangMapEntries(ActivityLangMapEntry.PROPNAME_NAME).toEmptyIfNull() +
                    this?.description?.toLangMapEntries(ActivityLangMapEntry.PROPNAME_DESCRIPTION).toEmptyIfNull() +
                    interactionEntitiesAndLangMaps.flatMap { it.second },
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

