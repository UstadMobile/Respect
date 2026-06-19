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

/**
 * Activity type for a class as per README_CLASS_RECIPE.md
 */
const val ACTIVITY_TYPE_CLASS = "http://id.openeel.org/xapi/activity-type/class"

/**
 * Category activity id for the class-management recipe as per README_CLASS_RECIPE.md
 */
const val CATEGORY_CLASS_MANAGEMENT = "https://id.openeel.org/xapi/recipes/class-management"


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
