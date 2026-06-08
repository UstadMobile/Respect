package world.respect.shared.domain.xapi

import kotlinx.serialization.json.JsonPrimitive
import world.respect.lib.xapi.OpenEelXapiConstants
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

@OptIn(ExperimentalUuidApi::class)
object XapiAppListingConstants {
    const val CATEGORY_APP_LISTING_RECIPE = OpenEelXapiConstants.CATEGORY_APP_LISTING_RECIPE
    const val EXT_WEBPUB_MANIFEST_LINK = OpenEelXapiConstants.ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK
    const val ACTIVITY_TYPE_APPLICATION = "http://activitystrea.ms/schema/1.0/application"
    const val VERB_LISTED_APP = XapiVerb.ID_LISTED_APP
}

@OptIn(ExperimentalUuidApi::class)
fun createBlankAppListingStatement(
    appActivityId: String,
    appTitle: String,
    actor: XapiActor,
    description: String? = null,
    moreInfo: String? = null,
    manifestUrl: String = appActivityId
): XapiStatement {
    val now = Clock.System.now()
    return XapiStatement(
        id = Uuid.random(),
        actor = actor,
        verb = XapiVerb(id = XapiAppListingConstants.VERB_LISTED_APP),
        `object` = XapiActivity(
            objectType = XapiObjectType.Activity,
            id = appActivityId,
            definition = XapiActivityDefinition(
                name = mapOf("en-US" to appTitle),
                description = description?.let { mapOf("en-US" to it) },
                type = XapiAppListingConstants.ACTIVITY_TYPE_APPLICATION,
                moreInfo = moreInfo,
                extensions = mapOf(
                    XapiAppListingConstants.EXT_WEBPUB_MANIFEST_LINK to JsonPrimitive(manifestUrl)
                )
            )
        ),
        context = XapiContext(
            contextActivities = XapiContextActivities(
                category = listOf(
                    XapiActivity(
                        id = XapiAppListingConstants.CATEGORY_APP_LISTING_RECIPE,
                        objectType = XapiObjectType.Activity
                    )
                ),
                grouping = emptyList()
            )
        ),
        timestamp = now,
        version = "1.0.0"
    )
}