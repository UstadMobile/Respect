package world.respect.datalayer.repository.school.xapi

import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.copyLoadState
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.local.XapiActivityProfileResourceLocal

/**
 * An offline-first repository implementation for XapiActivityProfileResource.
 */
class XapiActivityProfileResourceRepository(
    private val local: XapiActivityProfileResourceLocal,
    private val remote: XapiActivityProfileResource,
    private val remoteWriteQueue: RemoteWriteQueue,
): XapiActivityProfileResource {

    /**
     * Gets the local result first, then gets the remote result (using if-not-modified-since header).
     * If remote result is NoDataLoadedState(reason=NotModified), then just return the local result.
     *
     * If the remote result returns profile ids that are not listed locally, fetch each of them
     * from remote and use the updateLocal function to store them.
     *
     * If the remote result is missing profile ids (e.g. they may have been deleted, or might not
     * yet have been synced) then check the deleted profiles for the given params.
     *  i)  If the profileId is listed in the deleted profiles doc and its time is more recent than
     *      the local modified time, delete the data profile data locally.
     *  ii) If the profileId is not listed in the deleted profiles doc or the deletion time is before
     *      the last modified time of the local data, then do nothing (eg wait for the updated data
     *      to be written to the remote data source).
     */
    override suspend fun getMultipleDocuments(
        params: XapiActivityProfileResource.MultiDocParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<List<String>> {
        return local.getMultipleDocuments(params, dataLoadParams)
    }

    override suspend fun get(
        params: XapiActivityProfileResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams
    ): DataLoadState<XapiDocument> {
        val localState = local.get(params, dataLoadParams)

        val remoteState = remote.get(
            params = params,
            dataLoadParams = dataLoadParams.copy(
                requestHeaders = headers {
                    appendAll(dataLoadParams.requestHeaders)

                    /**
                     * Using the last-modified header here will prevent data just updated locally
                     * from being overwritten. The last-modified header on the server will be the
                     * time that data was actually stored on the server, NOT when it was actually
                     * modified on the client. It is however close enough.
                     */
                    localState.metaInfo.headers[HttpHeaders.LastModified]?.also {
                        set(HttpHeaders.IfModifiedSince, it)
                    }
                    localState.metaInfo.headers[HttpHeaders.ETag]?.also {
                        set(HttpHeaders.IfNoneMatch, it)
                    }
                }
            )
        )

        if(remoteState is DataReadyState) {
            local.updateLocal(params, remoteState.data)
            return local.get(params, dataLoadParams).copyLoadState(
                remoteState = remoteState
            )
        }

        return localState.copyLoadState(remoteState = remoteState)
    }

    override suspend fun post(
        params: XapiActivityProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        local.post(params, document)
    }

    override suspend fun put(
        params: XapiActivityProfileResource.SingleDocumentParams,
        document: XapiDocument
    ) {
        local.put(params, document)
    }

    override suspend fun delete(params: XapiActivityProfileResource.SingleDocumentParams) {
        TODO("Not yet implemented")
    }
}