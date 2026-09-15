package org.openeel.libxapi.test

import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

abstract class AbstractXapiActivityProfileResourceTest {

    abstract suspend fun withXapiActivityProfileResource(
        block: suspend (XapiActivityProfileResource) -> Unit
    )

    @Test
    fun givenDocument_whenPut_thenCanBeRetrieved() = runBlocking {
        withXapiActivityProfileResource { resource ->
            assertActivityProfileCanBePutAndRetrieved(
                resource = resource
            )
        }
    }

    /**
     * Check if the resource supports validation using the If-Modified-Since header parameter.
     *
     * Important: The Last-Modified timestamp on the resource will be the time the resource actually
     * stored it. When a mobile client stores a document, it's last-modified time will be when the
     * mobile client stored it. The last-modified time on the server will be when the server stored
     * it.
     */
    @Test
    fun givenDocumentStoredAndNotModified_whenRetrievedWithValidationHeaders_thenReturnsNotModified() = runBlocking {
        withXapiActivityProfileResource { resource ->
            val document = XapiActivityProfileTestParams.DOC
            resource.put(
                params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
                document = document
            )

            val initResponse = resource.get(
                params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
            )

            val initResponseLastMod = initResponse.metaInfo.headers[HttpHeaders.LastModified]
            assertNotNull(initResponseLastMod)

            val initResponseETag = initResponse.metaInfo.headers[HttpHeaders.ETag]
            assertNotNull(initResponseETag)

            val response = resource.get(
                params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
                dataLoadParams = DataLoadParams(
                    requestHeaders = headersOf(
                        HttpHeaders.IfModifiedSince to listOf(initResponseLastMod)
                    )
                )
            )

            assertIs<NoDataLoadedState<XapiDocument>>(response)
            assertEquals(NoDataLoadedState.Reason.NOT_MODIFIED, response.reason)

            val responseWithIfNoneMatch = resource.get(
                params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
                dataLoadParams = DataLoadParams(
                    requestHeaders = headersOf(
                        HttpHeaders.IfNoneMatch to listOf(initResponseETag)
                    )
                )
            )

            assertIs<NoDataLoadedState<XapiDocument>>(responseWithIfNoneMatch)
            assertEquals(NoDataLoadedState.Reason.NOT_MODIFIED, responseWithIfNoneMatch.reason)
        }
    }

    @Test
    fun givenNonExistentDocument_whenGetCalled_thenReturnsNotFound() = runBlocking {
        withXapiActivityProfileResource { resource ->
            assertNonExistentActivityProfileReturnsNotFound(
                resource = resource
            )
        }
    }

    @Test
    fun givenExistingDocument_whenOverwrittenWithPut_thenReplacesDocumentCompletely() = runBlocking {
        withXapiActivityProfileResource { resource ->
            assertActivityProfileOverwrittenWithPutReplacesDocumentCompletely(
                resource = resource
            )
        }
    }

    @Test
    fun givenNonExistentDocument_whenPosted_thenCreatesNewDocument() = runBlocking {
        withXapiActivityProfileResource { resource ->
            assertNonExistentActivityProfileWhenPostedCreatesNewDocument(
                resource = resource
            )
        }
    }

    @Test
    fun givenExistingJsonDocument_whenPosted_thenMergesTopLevelProperties() = runBlocking {
        withXapiActivityProfileResource { resource ->
            assertExistingActivityProfileJsonDocumentWhenPostedMergesTopLevelProperties(
                resource = resource
            )
        }
    }

    @Test
    fun givenNonJsonDocument_whenPostedToExisting_thenThrowsXapiException() = runBlocking {
        withXapiActivityProfileResource { resource ->
            assertNonJsonActivityProfileWhenPostedToExistingThrowsXapiException(
                resource = resource
            )
        }
    }

    @Test
    fun givenDocument_whenDeleted_thenCannotBeRetrieved() = runBlocking {
        withXapiActivityProfileResource { resource ->
            assertActivityProfileWhenDeletedCannotBeRetrieved(
                resource = resource
            )
        }
    }

    @Test
    fun givenMultipleDocuments_whenGetMultipleDocumentsCalled_thenReturnsAllProfileIdsForActivity() = runBlocking {
        withXapiActivityProfileResource { resource ->
            assertMultipleActivityProfileDocumentsReturnsAllProfileIdsForActivity(
                resource = resource
            )
        }
    }

    @Test
    fun givenMultipleDocumentsWithTimestamps_whenGetMultipleDocumentsWithSince_thenReturnsOnlyNewerProfileIds() = runBlocking {
        withXapiActivityProfileResource { resource ->
            assertMultipleActivityProfileDocumentsWithSinceReturnsOnlyNewerProfileIds(
                resource = resource
            )
        }
    }
}
