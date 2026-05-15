package world.respect.shared.domain.xapi

import io.ktor.http.Url
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.lib.xapi.model.*
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object XapiAssignmentMapper {

    const val CATEGORY_ASSIGNMENT_RECIPE = "https://id.ustadmobile.com/xapi/activities/assignment-recipe"
    const val EXT_DEADLINE = "https://id.ustadmobile.com/xapi/extension/deadline"
    const val EXT_CREATED = "https://id.ustadmobile.com/xapi/extension/created"
    const val EXT_APP_MANIFEST = "https://id.ustadmobile.com/xapi/extension/app-manifest"

    /**
     * Maps an xAPI Statement (Assignment Recipe) to the Assignment model.
     */
    fun toAssignment(statement: XapiStatement): Assignment? {
        if (statement.verb.id != VERB_ASSIGN) return null

        val context = statement.context ?: return null
        val categories = context.contextActivities?.category ?: emptyList()
        if (categories.none { it.id == CATEGORY_ASSIGNMENT_RECIPE }) return null

        val activity = statement.`object` as? XapiActivity ?: return null
        val definition = activity.definition ?: return null

        fun XapiActivityDefinition.getExtension(key: String): String? =
            extensions?.get(key)?.let { (it as? JsonPrimitive)?.contentOrNull }

        val deadline = definition.getExtension(EXT_DEADLINE)?.let { runCatching { Instant.parse(it) }.getOrNull() }
        val created = definition.getExtension(EXT_CREATED)?.let { runCatching { Instant.parse(it) }.getOrNull() }

        val learningUnits = context.contextActivities?.grouping?.mapNotNull { groupingActivity ->
            val manifestUrl = runCatching { Url(groupingActivity.id) }.getOrNull() ?: return@mapNotNull null

            val appUrlStr = groupingActivity.definition?.getExtension(EXT_APP_MANIFEST)
            val appUrl = appUrlStr?.let { runCatching { Url(it) }.getOrNull() } ?: manifestUrl

            AssignmentLearningUnitRef(
                learningUnitManifestUrl = manifestUrl,
                appManifestUrl = appUrl
            )
        } ?: emptyList()

        return Assignment(
            uid = activity.id.substringAfterLast("/"),
            title = definition.name?.get("en-US").orEmpty(),
            description = definition.description?.get("en-US").orEmpty(),
            deadline = deadline,
            classUid = statement.actor.account?.name.orEmpty(),
            learningUnits = learningUnits,
            lastModified = created ?: statement.timestamp ?: Clock.System.now(),
            stored = statement.stored ?: Clock.System.now()
        )
    }

    /**
     * Creates an xAPI Statement following the Assignment Recipe from an Assignment model.
     */
    fun fromAssignment(
        assignment: Assignment,
        schoolUrl: String,
        assignee: XapiActor,
        instructor: XapiActor
    ): XapiStatement {
        val schoolUrlClean = schoolUrl.trim().removeSuffix("/")
        val assignmentActivityId = "$schoolUrlClean/xapi/activities/assignment/${assignment.uid}"
        return XapiStatement(
            id = Uuid.random(),
            actor = assignee,
            verb = XapiVerb(
                id = VERB_ASSIGN,
                display = mapOf("en-US" to "assigned")
            ),
            `object` = XapiActivity(
                objectType = XapiObjectType.Activity,
                id = assignmentActivityId,
                definition = XapiActivityDefinition(
                    name = mapOf("en-US" to assignment.title),
                    description = mapOf("en-US" to assignment.description),
                    type = "http://id.tincanapi.com/activitytype/school-assignment",
                    extensions = buildMap {
                        assignment.deadline?.let { put(EXT_DEADLINE, JsonPrimitive(it.toString())) }
                        put(EXT_CREATED, JsonPrimitive(assignment.lastModified.toString()))
                    }
                )
            ),
            context = XapiContext(
                instructor = instructor,
                contextActivities = XapiContextActivities(
                    category = listOf(XapiActivity(id = CATEGORY_ASSIGNMENT_RECIPE, objectType = XapiObjectType.Activity)),
                    grouping = assignment.learningUnits.map { ref ->
                        XapiActivity(
                            id = ref.learningUnitManifestUrl.toString(),
                            objectType = XapiObjectType.Activity,
                            definition = XapiActivityDefinition(
                                extensions = mapOf(EXT_APP_MANIFEST to JsonPrimitive(ref.appManifestUrl.toString()))
                            )
                        )
                    }
                )
            ),
            timestamp = assignment.lastModified,
            version = "1.0.0"
        )
    }
}
