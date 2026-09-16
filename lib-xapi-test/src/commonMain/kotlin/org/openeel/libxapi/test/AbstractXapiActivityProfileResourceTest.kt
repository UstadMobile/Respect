package org.openeel.libxapi.test

import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.util.sha1
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.datetime.toGMTDate
import world.respect.lib.dataloadstate.datetime.toInstant
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.ext.decodeFromXapiDocument
import world.respect.lib.xapi.ext.getJson
import world.respect.lib.xapi.ext.mergeTopLevel
import world.respect.lib.xapi.ext.postJson
import world.respect.lib.xapi.ext.putJson
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

abstract class AbstractXapiActivityProfileResourceTest {

    abstract suspend fun withXapiActivityProfileResource(
        block: suspend (XapiActivityProfileResource) -> Unit
    )

    private val json = Json

    /**
     * Asserts that an activity profile document can be stored via [XapiActivityProfileResource.put]
     * and subsequently retrieved via [XapiActivityProfileResource.get].
     *
     * Verifies that:
     * - Storing the document via `put` completes successfully.
     * - Retrieving via `get` returns a [DataReadyState] containing the document.
     * - The retrieved document matches the stored document's content type, content bytes, and updated timestamp.
     *
     */
    @Test
    fun givenDocument_whenPut_thenCanBeRetrieved() = runBlocking {
        withXapiActivityProfileResource { resource ->
            val params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1
            val document = XapiActivityProfileTestParams.DOC

            resource.put(params, document)

            val loadState = resource.get(params)
            assertIs<DataReadyState<XapiDocument>>(loadState)

            val retrieved = loadState.data
            assertEquals(document.type, retrieved.type)
            assertContentEquals(
                document.contentsAsByteArray(),
                retrieved.contentsAsByteArray()
            )

            assertEquals(document.updated, retrieved.updated)
            val expectedEtag = sha1(document.contentsAsByteArray()).toHexString()
            assertEquals(expectedEtag, loadState.metaInfo.headers[HttpHeaders.ETag])
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

    /**
     * Asserts that attempting to retrieve a non-existent activity profile document returns [NoDataLoadedState].
     */
    @Test
    fun givenNonExistentDocument_whenGetCalled_thenReturnsNotFound() = runBlocking {
        withXapiActivityProfileResource { resource ->
            val loadState = resource.get(
                params = XapiActivityProfileTestParams.SINGLE_DOC_NON_EXISTENT_PARAMS
            )
            assertIs<NoDataLoadedState<XapiDocument>>(loadState)
            assertNull(loadState.dataOrNull())
        }
    }

    /**
     * Asserts that overwriting an existing activity profile document with [XapiActivityProfileResource.put]
     * replaces the entire document rather than merging properties.
     */
    @Test
    fun givenExistingDocument_whenOverwrittenWithPut_thenReplacesDocumentCompletely() = runBlocking {
        withXapiActivityProfileResource { resource ->
            val params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1
            val initialDocument = XapiActivityProfileTestParams.DOC_JSON
            val overwritingDocument = XapiActivityProfileTestParams.DOC_UPDATED_JSON

            resource.putJson(params, initialDocument, json, JsonObject.serializer())
            resource.putJson(params, overwritingDocument, json, JsonObject.serializer())

            val retrievedJson = resource.getJson(params, json, JsonObject.serializer())
            assertIs<DataReadyState<JsonObject>>(retrievedJson)
            val retrievedJsonObj = retrievedJson.data

            initialDocument.keys.filter { key ->
                key !in overwritingDocument.keys
            }.forEach { key ->
                assertNull(retrievedJsonObj[key])
            }

            assertEquals(
                expected = overwritingDocument,
                actual = retrievedJsonObj,
            )
        }
    }

    /**
     * Asserts that posting to a non-existent activity profile document creates a new document.
     */
    @Test
    fun givenNonExistentDocument_whenPosted_thenCreatesNewDocument() = runBlocking {
        withXapiActivityProfileResource { resource ->
            val params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1
            val document = XapiActivityProfileTestParams.DOC

            resource.post(params, document)

            val loadState = resource.get(params)
            val retrieved = loadState.dataOrNull()
            assertNotNull(retrieved)
            assertContentEquals(
                document.contentsAsByteArray(),
                retrieved.contentsAsByteArray()
            )
            val expectedEtag = sha1(document.contentsAsByteArray()).toHexString()
            assertEquals(expectedEtag, loadState.metaInfo.headers[HttpHeaders.ETag])
        }
    }

    /**
     * Asserts that posting a JSON document to an existing JSON activity profile document
     * merges top-level properties according to the xAPI specification.
     */
    @Test
    fun givenExistingJsonDocument_whenPosted_thenMergesTopLevelProperties() = runBlocking {
        withXapiActivityProfileResource { resource ->
            val params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1
            val initJsonObj = buildJsonObject {
                put("propA", JsonPrimitive("oldA"))
                put("propB", JsonPrimitive("keepB"))
                put(
                    "nested",
                    JsonObject(mapOf("sub1" to JsonPrimitive(100)))
                )
            }

            val updatedPosted = buildJsonObject {
                put("propA", JsonPrimitive("newA"))
                put("propC", JsonPrimitive("addedC"))
                put(
                    key = "nested",
                    element = JsonObject(mapOf("sub2" to JsonPrimitive(200)))
                )
            }

            resource.putJson(
                docParams = params,
                document = initJsonObj,
                json = json,
                serializer = JsonObject.serializer(),
            )

            resource.postJson(
                docParams = params,
                document = updatedPosted,
                json = json,
                serializer = JsonObject.serializer(),
            )

            val getResult = resource.get(params = params)
            val document = getResult.dataOrNull()
            assertNotNull(document)

            val jsonObjRetrieved = json.decodeFromXapiDocument(
                deserializer = JsonObject.serializer(),
                document = document,
            )

            assertEquals(
                expected = initJsonObj.mergeTopLevel(updatedPosted),
                actual = jsonObjRetrieved,
            )

            assertEquals(
                expected = sha1(document.contentsAsByteArray()).toHexString(),
                actual = getResult.metaInfo.headers[HttpHeaders.ETag]
            )
        }
    }

    /**
     * Asserts that posting a non-JSON document to an existing activity profile document
     * throws a [XapiException] with HTTP 400 Bad Request status.
     */
    @Test
    fun givenNonJsonDocument_whenPostedToExisting_thenThrowsXapiException() = runBlocking {
        withXapiActivityProfileResource { resource ->
            val params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1
            val document = XapiActivityProfileTestParams.DOC_NON_JSON

            resource.put(params, document)

            val exception = assertFailsWith<XapiException> {
                resource.post(params, document)
            }
            assertEquals(400, exception.httpStatusCode)
        }
    }

    /**
     * Asserts that an activity profile document can be deleted via [XapiActivityProfileResource.delete]
     * and that subsequent calls to [XapiActivityProfileResource.get] return [NoDataLoadedState].
     */
    @Test
    fun givenDocument_whenDeleted_thenCannotBeRetrieved() = runBlocking {
        withXapiActivityProfileResource { resource ->
            val params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1
            val document = XapiActivityProfileTestParams.DOC

            resource.put(params, document)
            assertNotNull(resource.get(params).dataOrNull())

            resource.delete(params)

            val loadState = resource.get(params)
            assertIs<NoDataLoadedState<XapiDocument>>(loadState)
            assertNull(loadState.dataOrNull())
        }
    }

    /**
     * Asserts that [XapiActivityProfileResource.getMultipleDocuments] returns all profile IDs
     * associated with a given activity ID.
     */
    @Test
    fun givenMultipleDocuments_whenGetMultipleDocumentsCalled_thenReturnsAllProfileIdsForActivity() = runBlocking {
        withXapiActivityProfileResource { resource ->
            val activityId1 = XapiActivityProfileTestParams.ACTIVITY_ID1
            val activityId2 = XapiActivityProfileTestParams.ACTIVITY_ID2

            val docs = listOf(activityId1, activityId2).flatMap { activityId ->
                (1..2).map { index ->
                    Pair(
                        first = XapiDocumentByteArrayImpl(
                            type = "application/json",
                            updated = Clock.System.now().toGMTDate(),
                            contents = """{"p": $index}""".encodeToByteArray(),
                        ),
                        second = XapiActivityProfileResource.SingleDocumentParams(
                            activityId = activityId,
                            profileId = "p$index",
                        )
                    )
                }
            }

            docs.forEach {
                resource.put(it.second, it.first)
            }

            val profileIds = resource.getMultipleDocuments(
                params = XapiActivityProfileResource.MultiDocParams(
                    activityId = activityId1
                )
            ).dataOrNull()
            assertNotNull(profileIds)

            assertEquals(
                expected = docs.filter { it.second.activityId == activityId1 }.map {
                    it.second.profileId
                }.toSet(),
                actual = profileIds.toSet()
            )
        }
    }

    /**
     * Asserts that [XapiActivityProfileResource.getMultipleDocuments] with a `since` parameter
     * returns only profile IDs for documents updated after the given timestamp.
     */
    @Test
    fun givenMultipleDocumentsWithTimestamps_whenGetMultipleDocumentsWithSince_thenReturnsOnlyNewerProfileIds() = runBlocking {
        withXapiActivityProfileResource { resource ->
            val activityId = XapiActivityProfileTestParams.ACTIVITY_ID1
            val baseTime = Instant.parse("2026-01-01T10:00:00Z")

            val testDocs = (1..2).mapIndexed { index, profileNum ->
                val updated = baseTime + (index * 10).seconds
                Pair(
                    first = XapiDocumentByteArrayImpl(
                        type = "application/json",
                        updated = updated.toGMTDate(),
                        contents = """{"p": $profileNum}""".encodeToByteArray(),
                    ),
                    second = XapiActivityProfileResource.SingleDocumentParams(
                        activityId = activityId,
                        profileId = "p$profileNum",
                    )
                )
            }
            testDocs.forEach {
                resource.put(
                    params = it.second,
                    document = it.first
                )
            }

            testDocs.forEach { doc ->
                assertEquals(
                    expected = testDocs.filter {
                        it.first.updated > doc.first.updated
                    }.map { it.second.profileId }.toSet(),
                    actual = resource.getMultipleDocuments(
                        params = XapiActivityProfileResource.MultiDocParams(
                            activityId = activityId,
                            since = doc.first.updated.toInstant(),
                        ),
                    ).dataOrNull()?.toSet()
                )
            }
        }
    }
}
