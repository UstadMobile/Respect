package org.openeel.libxapi.test

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.ext.getJson
import world.respect.lib.xapi.ext.mergeTopLevel
import world.respect.lib.xapi.ext.postJson
import world.respect.lib.xapi.ext.putJson
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Common test parameters and example documents for xAPI Activity Profile resource tests.
 */
object XapiActivityProfileTestParams {
    const val ACTIVITY_ID1 = "http://example.com/activities/course-1"
    const val ACTIVITY_ID2 = "http://example.com/activities/course-2"
    const val PROFILE_ID1 = "profile-1"
    const val PROFILE_ID_NON_EXISTENT = "non-existent-profile"

    val SINGLE_DOC_PARAMS1 = XapiActivityProfileResource.SingleDocumentParams(
        activityId = ACTIVITY_ID1,
        profileId = PROFILE_ID1,
    )

    val SINGLE_DOC_NON_EXISTENT_PARAMS = XapiActivityProfileResource.SingleDocumentParams(
        activityId = ACTIVITY_ID1,
        profileId = PROFILE_ID_NON_EXISTENT,
    )

    val DOC: XapiDocument = XapiDocumentByteArrayImpl(
        type = "application/json",
        updated = Clock.System.now(),
        contents = """{"initialKey": "initialValue", "nested": {"a": 1}}""".encodeToByteArray(),
    )

    val DOC_JSON = buildJsonObject {
        put("initialKey", JsonPrimitive("initialValue"))
        put("nested", buildJsonObject {
            put("a", JsonPrimitive(1))
        })
    }

    val DOC_UPDATED: XapiDocument = XapiDocumentByteArrayImpl(
        type = "application/json",
        updated = Clock.System.now(),
        contents = """{"newKey": "newValue"}""".encodeToByteArray(),
    )

    val DOC_UPDATED_JSON = buildJsonObject {
        put("newKey", JsonPrimitive("newValue"))
    }

    val DOC_NON_JSON: XapiDocument = XapiDocumentByteArrayImpl(
        type = "text/plain",
        updated = Clock.System.now(),
        contents = "plain text content".encodeToByteArray(),
    )
}

/**
 * Asserts that an activity profile document can be stored via [XapiActivityProfileResource.put]
 * and subsequently retrieved via [XapiActivityProfileResource.get].
 *
 * Verifies that:
 * - Storing the document via `put` completes successfully.
 * - Retrieving via `get` returns a [DataReadyState] containing the document.
 * - The retrieved document matches the stored document's content type, content bytes, and updated timestamp.
 *
 * @param resource The [XapiActivityProfileResource] under test.
 * @param params The document parameters identifying the activity and profile.
 * @param document The document to store and retrieve.
 */
suspend fun assertActivityProfileCanBePutAndRetrieved(
    resource: XapiActivityProfileResource,
    params: XapiActivityProfileResource.SingleDocumentParams = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
    document: XapiDocument = XapiActivityProfileTestParams.DOC,
) {
    resource.put(params, document)

    val loadState = resource.get(params)
    assertIs<DataReadyState<XapiDocument>>(loadState)

    val retrieved = loadState.data
    assertEquals(document.type, retrieved.type)
    assertContentEquals(
        document.contentsAsByteArray(),
        retrieved.contentsAsByteArray()
    )
    assertEquals(document.updated.toEpochMilliseconds(), retrieved.updated.toEpochMilliseconds())
}

/**
 * Asserts that attempting to retrieve a non-existent activity profile document returns [NoDataLoadedState].
 *
 * @param resource The [XapiActivityProfileResource] under test.
 * @param params The document parameters for the non-existent document.
 */
suspend fun assertNonExistentActivityProfileReturnsNotFound(
    resource: XapiActivityProfileResource,
    params: XapiActivityProfileResource.SingleDocumentParams = XapiActivityProfileTestParams.SINGLE_DOC_NON_EXISTENT_PARAMS,
) {
    val loadState = resource.get(params)
    assertIs<NoDataLoadedState<XapiDocument>>(loadState)
    assertNull(loadState.dataOrNull())
}

/**
 * Asserts that overwriting an existing activity profile document with [XapiActivityProfileResource.put]
 * replaces the entire document rather than merging properties.
 *
 * @param resource The [XapiActivityProfileResource] under test.
 * @param params The document parameters.
 * @param initialDocument The initial document to store.
 * @param overwritingDocument The document that overwrites the initial document.
 */
suspend fun assertActivityProfileOverwrittenWithPutReplacesDocumentCompletely(
    resource: XapiActivityProfileResource,
    params: XapiActivityProfileResource.SingleDocumentParams = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
    initialDocument: JsonObject = XapiActivityProfileTestParams.DOC_JSON,
    overwritingDocument: JsonObject = XapiActivityProfileTestParams.DOC_UPDATED_JSON,
    json: Json = Json,
) {
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

/**
 * Asserts that posting to a non-existent activity profile document creates a new document.
 *
 * @param resource The [XapiActivityProfileResource] under test.
 * @param params The document parameters.
 * @param document The document to post.
 */
suspend fun assertNonExistentActivityProfileWhenPostedCreatesNewDocument(
    resource: XapiActivityProfileResource,
    params: XapiActivityProfileResource.SingleDocumentParams = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
    document: XapiDocument = XapiActivityProfileTestParams.DOC,
) {
    resource.post(params, document)

    val loadState = resource.get(params)
    val retrieved = loadState.dataOrNull()
    assertNotNull(retrieved)
    assertContentEquals(
        document.contentsAsByteArray(),
        retrieved.contentsAsByteArray()
    )
}

/**
 * Asserts that posting a JSON document to an existing JSON activity profile document
 * merges top-level properties according to the xAPI specification.
 *
 * @param resource The [XapiActivityProfileResource] under test.
 * @param params The document parameters.
 * @param json The [Json] instance to use for serialization operations.
 */
suspend fun assertExistingActivityProfileJsonDocumentWhenPostedMergesTopLevelProperties(
    resource: XapiActivityProfileResource,
    params: XapiActivityProfileResource.SingleDocumentParams = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
    json: Json = Json { encodeDefaults = false },
) {
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

    val jsonObjRetrieved = resource.getJson(
        docParams = params,
        json = json,
        deserializer = JsonObject.serializer(),
    ).dataOrNull()
    assertNotNull(jsonObjRetrieved)

    assertEquals(
        expected = initJsonObj.mergeTopLevel(updatedPosted),
        actual = jsonObjRetrieved,
    )
}

/**
 * Asserts that posting a non-JSON document to an existing activity profile document
 * throws a [XapiException] with HTTP 400 Bad Request status.
 *
 * @param resource The [XapiActivityProfileResource] under test.
 * @param params The document parameters.
 * @param document The non-JSON document to put and post.
 */
suspend fun assertNonJsonActivityProfileWhenPostedToExistingThrowsXapiException(
    resource: XapiActivityProfileResource,
    params: XapiActivityProfileResource.SingleDocumentParams = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
    document: XapiDocument = XapiActivityProfileTestParams.DOC_NON_JSON,
) {
    resource.put(params, document)

    val exception = assertFailsWith<XapiException> {
        resource.post(params, document)
    }
    assertEquals(400, exception.httpStatusCode)
}

/**
 * Asserts that an activity profile document can be deleted via [XapiActivityProfileResource.delete]
 * and that subsequent calls to [XapiActivityProfileResource.get] return [NoDataLoadedState].
 *
 * @param resource The [XapiActivityProfileResource] under test.
 * @param params The document parameters.
 * @param document The document to store before deletion.
 */
suspend fun assertActivityProfileWhenDeletedCannotBeRetrieved(
    resource: XapiActivityProfileResource,
    params: XapiActivityProfileResource.SingleDocumentParams = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1,
    document: XapiDocument = XapiActivityProfileTestParams.DOC,
) {
    resource.put(params, document)
    assertNotNull(resource.get(params).dataOrNull())

    resource.delete(params)

    val loadState = resource.get(params)
    assertIs<NoDataLoadedState<XapiDocument>>(loadState)
    assertNull(loadState.dataOrNull())
}

/**
 * Asserts that [XapiActivityProfileResource.getMultipleDocuments] returns all profile IDs
 * associated with a given activity ID.
 *
 * @param resource The [XapiActivityProfileResource] under test.
 * @param activityId1 The target activity ID to query profile IDs for.
 * @param activityId2 Another activity ID stored in the resource to verify filtering.
 */
suspend fun assertMultipleActivityProfileDocumentsReturnsAllProfileIdsForActivity(
    resource: XapiActivityProfileResource,
    activityId1: String = XapiActivityProfileTestParams.ACTIVITY_ID1,
    activityId2: String = XapiActivityProfileTestParams.ACTIVITY_ID2,
) {
    val docs = listOf(activityId1, activityId2).flatMap { activityId ->
        (1..2).map { index ->
            Pair(
                first = XapiDocumentByteArrayImpl(
                    type = "application/json",
                    updated = Clock.System.now(),
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

/**
 * Asserts that [XapiActivityProfileResource.getMultipleDocuments] with a `since` parameter
 * returns only profile IDs for documents updated after the given timestamp.
 *
 * @param resource The [XapiActivityProfileResource] under test.
 * @param activityId The activity ID to query profile IDs for.
 */
suspend fun assertMultipleActivityProfileDocumentsWithSinceReturnsOnlyNewerProfileIds(
    resource: XapiActivityProfileResource,
    activityId: String = XapiActivityProfileTestParams.ACTIVITY_ID1,
) {
    val baseTime = Instant.parse("2026-01-01T10:00:00Z")

    val testDocs = (1..2).mapIndexed { index, profileNum ->
        val updated = baseTime + (index * 10).seconds
        Pair(
            first = XapiDocumentByteArrayImpl(
                type = "application/json",
                updated = updated,
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
                    since = doc.first.updated,
                ),
            ).dataOrNull()?.toSet()
        )
    }
}
