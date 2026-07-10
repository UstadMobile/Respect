package world.respect.shared.domain.opds.getxapiactivityid

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import world.respect.datalayer.school.opds.ext.requireAbsoluteSelfUrl
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.findLearningUnitAcquisitionLinks
import world.respect.lib.opds.model.toStringMap
import world.respect.lib.xapi.OpenEelXapiConstants.ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.libutil.ext.resolve

/**
 * Generate an XapiActivity (including definition) for a given publication.
 */
class GetXapiActivityForPublicationUseCase {

    //Will likely need to use suspended functions in future
    @Suppress("RedundantSuspendModifier")
    suspend operator fun invoke(
        publication: OpdsPublication
    ) : XapiActivity {
        val publicationUrl = publication.requireAbsoluteSelfUrl()

        val activityId = publication.metadata.identifier?.toString()
            ?: publication.findLearningUnitAcquisitionLinks().firstOrNull()?.let {
                publicationUrl.resolve(it.href)
            }?.toString() ?: throw IllegalArgumentException("Cannot determine xAPI activityId for publication")

        return XapiActivity(
            id = activityId,
            definition = XapiActivityDefinition(
                name = publication.metadata.title.toStringMap(noLangKey = "en-US"),
                extensions = JsonObject(
                    mapOf(ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK to JsonPrimitive(publicationUrl.toString()))
                )
            )
        )
    }


}