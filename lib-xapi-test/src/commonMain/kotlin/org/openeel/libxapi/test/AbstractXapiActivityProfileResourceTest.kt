package org.openeel.libxapi.test

import kotlinx.coroutines.runBlocking
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import kotlin.test.Test

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
