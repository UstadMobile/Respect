package world.respect.shared.domain.xapi

import io.ktor.http.Url
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.lib.xapi.model.*
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object XapiAssignmentConstants {
    const val CATEGORY_ASSIGNMENT_RECIPE = "https://id.ustadmobile.com/xapi/activities/assignment-recipe"
    const val EXT_DEADLINE = "https://id.ustadmobile.com/xapi/extension/deadline"
    const val EXT_CREATED = "https://id.ustadmobile.com/xapi/extension/created"
    const val EXT_APP_MANIFEST = "https://id.ustadmobile.com/xapi/extension/app-manifest"
    const val ACTIVITY_TYPE_ASSIGNMENT = "http://id.tincanapi.com/activitytype/school-assignment"
    const val ACTIVITY_ID_PATH_PREFIX = "/xapi/activities/assignment/"
}

@OptIn(ExperimentalUuidApi::class)
val XapiStatement.isAssignmentStatement: Boolean
    get() {
        if (verb.id != VERB_ASSIGN) return false
        val categories = context?.contextActivities?.category ?: emptyList()
        return categories.any { it.id == XapiAssignmentConstants.CATEGORY_ASSIGNMENT_RECIPE }
    }

@OptIn(ExperimentalUuidApi::class)
val XapiStatement.assignmentUid: String?
    get() = (this.`object` as? XapiActivity)?.id?.substringAfterLast("/")

@OptIn(ExperimentalUuidApi::class)
val XapiStatement.assignmentTitle: String
    get() = (this.`object` as? XapiActivity)?.definition?.name?.get("en-US") ?: ""

@OptIn(ExperimentalUuidApi::class)
val XapiStatement.assignmentDescription: String
    get() = (this.`object` as? XapiActivity)?.definition?.description?.get("en-US") ?: ""

@OptIn(ExperimentalUuidApi::class)
val XapiStatement.assignmentClassUid: String
    get() = this.actor.account?.name.orEmpty()

@OptIn(ExperimentalUuidApi::class)
val XapiStatement.assignmentClassName: String
    get() = this.actor.name.orEmpty()

@OptIn(ExperimentalUuidApi::class)
val XapiStatement.assignmentDeadline: Instant?
    get() = (this.`object` as? XapiActivity)?.definition?.extensions?.get(XapiAssignmentConstants.EXT_DEADLINE)
        ?.let { (it as? JsonPrimitive)?.contentOrNull }
        ?.let { runCatching { Instant.parse(it) }.getOrNull() }

@OptIn(ExperimentalUuidApi::class)
val XapiStatement.assignmentLearningUnits: List<AssignmentLearningUnitRef>
    get() = context?.contextActivities?.grouping?.mapNotNull { groupingActivity ->
        val manifestUrl = runCatching { Url(groupingActivity.id) }.getOrNull() ?: return@mapNotNull null
        val appUrlStr = (groupingActivity.definition?.extensions?.get(XapiAssignmentConstants.EXT_APP_MANIFEST) as? JsonPrimitive)?.contentOrNull
        val appUrl = appUrlStr?.let { runCatching { Url(it) }.getOrNull() } ?: manifestUrl
        AssignmentLearningUnitRef(manifestUrl, appUrl)
    } ?: emptyList()

@OptIn(ExperimentalUuidApi::class)
fun XapiStatement.withTitle(title: String): XapiStatement {
    val activity = `object` as? XapiActivity ?: return this
    val definition = activity.definition ?: XapiActivityDefinition()
    return copy(
        `object` = activity.copy(
            definition = definition.copy(
                name = (definition.name ?: emptyMap()) + ("en-US" to title)
            )
        )
    )
}

@OptIn(ExperimentalUuidApi::class)
fun XapiStatement.withDescription(description: String): XapiStatement {
    val activity = `object` as? XapiActivity ?: return this
    val definition = activity.definition ?: XapiActivityDefinition()
    return copy(
        `object` = activity.copy(
            definition = definition.copy(
                description = (definition.description ?: emptyMap()) + ("en-US" to description)
            )
        )
    )
}

@OptIn(ExperimentalUuidApi::class)
fun XapiStatement.withDeadline(deadline: Instant?): XapiStatement {
    val activity = `object` as? XapiActivity ?: return this
    val definition = activity.definition ?: XapiActivityDefinition()
    val newExtensions = (definition.extensions ?: emptyMap()).toMutableMap().apply {
        if (deadline != null) {
            put(XapiAssignmentConstants.EXT_DEADLINE, JsonPrimitive(deadline.toString()))
        } else {
            remove(XapiAssignmentConstants.EXT_DEADLINE)
        }
    }
    return copy(
        `object` = activity.copy(
            definition = definition.copy(extensions = newExtensions)
        )
    )
}

@OptIn(ExperimentalUuidApi::class)
fun XapiStatement.withClass(classUid: String, className: String, schoolUrl: String): XapiStatement {
    val newActor = XapiGroup(
        name = className,
        account = XapiAccount(
            homePage = schoolUrl,
            name = classUid
        ),
        objectType = XapiObjectType.Group
    )
    return copy(actor = newActor)
}

@OptIn(ExperimentalUuidApi::class)
fun XapiStatement.withLearningUnits(learningUnits: List<AssignmentLearningUnitRef>): XapiStatement {
    val newGrouping = learningUnits.map { ref ->
        XapiActivity(
            id = ref.learningUnitManifestUrl.toString(),
            objectType = XapiObjectType.Activity,
            definition = XapiActivityDefinition(
                extensions = mapOf(XapiAssignmentConstants.EXT_APP_MANIFEST to JsonPrimitive(ref.appManifestUrl.toString()))
            )
        )
    }
    val newContext = (context ?: XapiContext()).copy(
        contextActivities = (context?.contextActivities ?: XapiContextActivities()).copy(
            grouping = newGrouping
        )
    )
    return copy(context = newContext)
}

@OptIn(ExperimentalUuidApi::class)
fun XapiStatement.withInstructor(instructor: XapiActor): XapiStatement {
    return copy(
        context = (context ?: XapiContext()).copy(instructor = instructor)
    )
}

@OptIn(ExperimentalUuidApi::class)
fun createBlankAssignmentStatement(
    uid: String,
    schoolUrl: String,
    instructor: XapiActor
): XapiStatement {
    val schoolUrlClean = schoolUrl.trim().removeSuffix("/")
    val assignmentActivityId = "$schoolUrlClean${XapiAssignmentConstants.ACTIVITY_ID_PATH_PREFIX}$uid"
    val now = kotlin.time.Clock.System.now()
    return XapiStatement(
        id = Uuid.random(),
        actor = XapiGroup(name = "", objectType = XapiObjectType.Group),
        verb = XapiVerb(
            id = VERB_ASSIGN,
            display = mapOf("en-US" to "assigned")
        ),
        `object` = XapiActivity(
            objectType = XapiObjectType.Activity,
            id = assignmentActivityId,
            definition = XapiActivityDefinition(
                name = mapOf("en-US" to ""),
                description = mapOf("en-US" to ""),
                type = XapiAssignmentConstants.ACTIVITY_TYPE_ASSIGNMENT,
                extensions = mapOf(XapiAssignmentConstants.EXT_CREATED to JsonPrimitive(now.toString()))
            )
        ),
        context = XapiContext(
            instructor = instructor,
            contextActivities = XapiContextActivities(
                category = listOf(XapiActivity(id = XapiAssignmentConstants.CATEGORY_ASSIGNMENT_RECIPE, objectType = XapiObjectType.Activity)),
                grouping = emptyList()
            )
        ),
        timestamp = now,
        version = "1.0.0"
    )
}
