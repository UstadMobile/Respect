package world.respect.shared.domain.xapi

import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val XAPI_LANG_KEY = ""

/**
 * Activity type for a class as per README_CLASS_RECIPE.md
 */
const val ACTIVITY_TYPE_CLASS = "http://id.openeel.org/xapi/activity-type/class"

/**
 * Category activity id for the class-management recipe as per README_CLASS_RECIPE.md
 */
const val CATEGORY_CLASS_MANAGEMENT = "https://id.openeel.org/xapi/recipes/class-management"

const val ACTIVITY_ID_PATH = "xapi/activities/classes"


/**
 * The activity definition name for a class statement.
 */
@OptIn(ExperimentalUuidApi::class)
val XapiStatement.classDefinitionTitle: String
    get() = (this.`object` as? XapiActivity)?.definition?.name?.values?.firstOrNull() ?: ""

/**
 * The activity definition description for a class statement.
 */
val XapiStatement.classDefinitionDescription: String
    get() = (this.`object` as? XapiActivity)?.definition?.description?.values?.firstOrNull() ?: ""


private fun XapiStatement.withDefinitionField(
    update: (XapiActivityDefinition) -> XapiActivityDefinition
): XapiStatement {
    val activity = `object` as? XapiActivity
        ?: throw IllegalStateException("Statement object is not an XapiActivity")
    val definition = activity.definition ?: XapiActivityDefinition()
    return copy(
        `object` = activity.copy(definition = update(definition))
    )
}

/**
 * Create a copy of this class statement with an updated title in the activity definition name.
 */
fun XapiStatement.withClassTitle(title: String): XapiStatement =
    withDefinitionField { def ->
        def.copy(name = (def.name ?: emptyMap()) + (XAPI_LANG_KEY to title))
    }

/**
 * Create a copy of this class statement with an updated description in the activity definition.
 */
fun XapiStatement.withClassDescription(description: String): XapiStatement =
    withDefinitionField { def ->
        def.copy(description = (def.description ?: emptyMap()) + (XAPI_LANG_KEY to description))
    }


/**
 * Create a blank class statement as per the class-management recipe.
 */
@OptIn(ExperimentalUuidApi::class)
fun createBlankClassStatement(
    classActivityId: String,
    actor: XapiActor
): XapiStatement {
    val now = Clock.System.now()
    return XapiStatement(
        id = Uuid.random(),
        actor = actor,
        verb = XapiVerb(
            id = XapiVerb.ID_SAVED,
        ),
        `object` = XapiActivity(
            objectType = XapiObjectType.Activity,
            id = classActivityId,
            definition = XapiActivityDefinition(
                name = emptyMap(),
                description = emptyMap(),
                type = ACTIVITY_TYPE_CLASS,
            )
        ),
        context = XapiContext(
            contextActivities = XapiContextActivities(
                category = listOf(
                    XapiActivity(
                        id = CATEGORY_CLASS_MANAGEMENT,
                        objectType = XapiObjectType.Activity
                    )
                ),
            ),
        ),
        timestamp = now,
        version = "1.0.0"
    )
}
