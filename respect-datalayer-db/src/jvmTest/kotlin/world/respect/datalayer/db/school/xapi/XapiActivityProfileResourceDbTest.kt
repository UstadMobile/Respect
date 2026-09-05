package world.respect.datalayer.db.school.xapi

import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.db.school.insertAdmin
import world.respect.datalayer.db.school.testSchoolDb
import world.respect.datalayer.db.school.toDataSource
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.ext.mergeTopLevel
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class XapiActivityProfileResourceDbTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private class TestDocument(
        override val type: String = "application/json",
        override val updated: Instant = Clock.System.now(),
        private val contents: ByteArray,
    ) : XapiDocument {

        constructor(
            type: String = "application/json",
            updated: Instant = Clock.System.now(),
            text: String,
        ) : this(
            type = type,
            updated = updated,
            contents = text.encodeToByteArray(),
        )

        override suspend fun contentsAsByteArray(): ByteArray = contents
    }

    private fun makeTestDocs(
        activityIds: List<String>,
        profileIds: List<String> = (1..2).map { "p$it" },
        updated: (Int) -> Instant = { Clock.System.now() },
    ) : List<Pair<TestDocument, XapiActivityProfileResource.ActivityProfileDocumentParams>> {
        return activityIds.flatMap { activityId ->
            profileIds.mapIndexed { index, profileId ->
                Pair(
                    first = TestDocument(
                        type = "application/json",
                        updated = updated(index),
                        text = """{"p": $index}"""
                    ),
                    second = XapiActivityProfileResource.ActivityProfileDocumentParams(
                        activityId = activityId,
                        profileId = profileId,
                    )
                )
            }
        }
    }


    private val activityId1 = "http://example.com/activities/course-1"
    private val activityId2 = "http://example.com/activities/course-2"
    private val profileId1 = "profile-1"

    private val json = Json { encodeDefaults = false }

    @Test
    fun givenDocument_whenPut_thenCanBeRetrieved() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.activityProfile
            val jsonContent = """{"score": 95, "completed": true}"""
            val timestamp = Clock.System.now()
            val document = TestDocument(
                type = "application/json",
                updated = timestamp,
                text = jsonContent,
            )

            val params = XapiActivityProfileResource.ActivityProfileDocumentParams(
                activityId = activityId1,
                profileId = profileId1,
            )

            resource.put(params, document)

            val loadState = resource.get(params)
            assertIs<DataReadyState<XapiDocument>>(loadState)

            val retrieved = loadState.data
            assertEquals("application/json", retrieved.type)
            assertEquals(jsonContent, retrieved.contentsAsByteArray().decodeToString())
            assertEquals(timestamp.toEpochMilliseconds(), retrieved.updated.toEpochMilliseconds())
        }
    }

    @Test
    fun givenNonExistentDocument_whenGetCalled_thenReturnsNotFound() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.activityProfile
            val params = XapiActivityProfileResource.ActivityProfileDocumentParams(
                activityId = activityId1,
                profileId = "non-existent-profile",
            )

            val loadState = resource.get(params)
            assertIs<NoDataLoadedState<XapiDocument>>(loadState)
            assertNull(loadState.dataOrNull())
        }
    }

    @Test
    fun givenExistingDocument_whenOverwrittenWithPut_thenReplacesDocumentCompletely() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.activityProfile
            val params = XapiActivityProfileResource.ActivityProfileDocumentParams(
                activityId = activityId1,
                profileId = profileId1,
            )

            resource.put(
                params = params,
                document = TestDocument(
                    type = "application/json",
                    text = """{"initialKey": "initialValue", "nested": {"a": 1}}""",
                )
            )

            val updatedDoc = TestDocument(
                type = "application/json",
                text = """{"newKey": "newValue"}""",
            )
            resource.put(params, updatedDoc)

            val loadState = resource.get(params)
            val retrieved = loadState.dataOrNull()
            assertNotNull(retrieved)

            val retrievedJson = Json.parseToJsonElement(
                retrieved.contentsAsByteArray().decodeToString()
            ) as JsonObject

            assertNull(retrievedJson["initialKey"])
            assertNull(retrievedJson["nested"])
            assertEquals("newValue", retrievedJson["newKey"]?.jsonPrimitive?.content)
        }
    }

    @Test
    fun givenNonExistentDocument_whenPosted_thenCreatesNewDocument() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.activityProfile
            val params = XapiActivityProfileResource.ActivityProfileDocumentParams(
                activityId = activityId1,
                profileId = profileId1,
            )

            val doc = TestDocument(
                type = "application/json",
                text = """{"key1": "val1"}""",
            )
            resource.post(params, doc)

            val loadState = resource.get(params)
            val retrieved = loadState.dataOrNull()
            assertNotNull(retrieved)
            assertEquals("""{"key1": "val1"}""", retrieved.contentsAsByteArray().decodeToString())
        }
    }

    @Test
    fun givenExistingJsonDocument_whenPosted_thenMergesTopLevelProperties() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.activityProfile
            val params = XapiActivityProfileResource.ActivityProfileDocumentParams(
                activityId = activityId1,
                profileId = profileId1,
            )

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

            resource.put(
                params = params,
                document = TestDocument(
                    contents = json.encodeToString(
                        JsonObject.serializer(), initJsonObj
                    ).encodeToByteArray()
                )
            )

            resource.post(
                params = params,
                document = TestDocument(
                    contents = json.encodeToString(
                        JsonObject.serializer(), updatedPosted
                    ).encodeToByteArray()
                )
            )

            val docRetrieved = resource.get(
                params = params,
            ).dataOrNull()
            assertNotNull(docRetrieved)

            val jsonObjRetrieved = json.decodeFromString(
                JsonObject.serializer(),
                docRetrieved.contentsAsByteArray().decodeToString()
            )

            assertEquals(
                expected = initJsonObj.mergeTopLevel(updatedPosted),
                actual = jsonObjRetrieved,
            )
        }
    }

    @Test
    fun givenNonJsonDocument_whenPostedToExisting_thenThrowsXapiException() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.activityProfile
            val params = XapiActivityProfileResource.ActivityProfileDocumentParams(
                activityId = activityId1,
                profileId = profileId1,
            )

            val textDoc = TestDocument(
                type = "text/plain",
                text = "plain text content",
            )
            resource.put(params, textDoc)

            val exception = assertFailsWith<XapiException> {
                resource.post(
                    params, textDoc
                )
            }
            assertEquals(400, exception.httpStatusCode)
        }
    }

    @Test
    fun givenDocument_whenDeleted_thenCannotBeRetrieved() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.activityProfile
            val params = XapiActivityProfileResource.ActivityProfileDocumentParams(
                activityId = activityId1,
                profileId = profileId1,
            )

            val doc = TestDocument(
                type = "application/json",
                text = """{"test": true}""",
            )
            resource.put(params, doc)

            assertNotNull(resource.get(params).dataOrNull())

            resource.delete(params)

            val loadState = resource.get(params)
            assertIs<NoDataLoadedState<XapiDocument>>(loadState)
            assertNull(loadState.dataOrNull())
        }
    }

    @Test
    fun givenMultipleDocuments_whenGetMultipleDocumentsCalled_thenReturnsAllProfileIdsForActivity() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.activityProfile

            val docs = makeTestDocs(
                activityIds = listOf(activityId1, activityId2)
            )

            docs.forEach {
                resource.put(it.second, it.first)
            }

            val profileIds = resource.getMultipleDocuments(
                params = XapiActivityProfileResource.GetActivityProfilesParams(
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

    @Test
    fun givenMultipleDocumentsWithTimestamps_whenGetMultipleDocumentsWithSince_thenReturnsOnlyNewerProfileIds() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.activityProfile
            val baseTime = Instant.parse("2026-01-01T10:00:00Z")

            val testDocs = makeTestDocs(
                activityIds = listOf(activityId1),
                updated = { index -> baseTime + (index * 10).seconds }
            )
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
                        params = XapiActivityProfileResource.GetActivityProfilesParams(
                            activityId = activityId1,
                            since = doc.first.updated,
                        ),
                    ).dataOrNull()?.toSet()
                )
            }
        }
    }

    @Test
    fun givenDocument_whenUpdateLocalCalled_thenCanBeRetrieved() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.activityProfile
            val params = XapiActivityProfileResource.ActivityProfileDocumentParams(
                activityId = activityId1,
                profileId = profileId1,
            )
            val timestamp = Clock.System.now()
            val doc = TestDocument(
                type = "application/json",
                updated = timestamp,
                text = """{"synced": true}""",
            )

            resource.updateLocal(params, doc)

            val retrieved = resource.get(params).dataOrNull()
            assertNotNull(retrieved)
            assertEquals("""{"synced": true}""", retrieved.contentsAsByteArray().decodeToString())
            assertEquals("application/json", retrieved.type)
            assertEquals(timestamp.toEpochMilliseconds(), retrieved.updated.toEpochMilliseconds())
        }
    }

}
