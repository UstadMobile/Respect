package world.respect.datalayer.school.xapi

import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.XapiActivityProfileResource.ActivityProfileDocumentParams

interface XapiActivityProfileResourceLocal : XapiActivityProfileResource {

    /**
     * As per other resources: update the given data locally, DO NOT enqueue for writing to the
     * remote endpoint or perform permission checks. This is used by the repository layer.
     */
    suspend fun updateLocal(
        params: ActivityProfileDocumentParams,
        document: XapiDocument,
    )

}