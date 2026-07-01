package world.respect.shared.domain.xapi

import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import kotlin.time.Clock
import kotlin.uuid.Uuid

fun createLearningUnitStatement(
    activityId: String,
    actor: XapiActor,
    verbId: String,
): XapiStatement {
    val now = Clock.System.now()

    return XapiStatement(
        id = Uuid.random(),
        actor = actor,
        verb = XapiVerb(
            id = verbId,
        ),
        `object` = XapiActivity(
            objectType = XapiObjectType.Activity,
            id = activityId,
        ),
        timestamp = now,
        version = "1.0.0",
    )
}