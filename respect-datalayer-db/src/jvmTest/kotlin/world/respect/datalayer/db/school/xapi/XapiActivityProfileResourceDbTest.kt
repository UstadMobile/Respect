package world.respect.datalayer.db.school.xapi

import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.assertActivityProfileCanBePutAndRetrieved
import org.openeel.libxapi.test.assertActivityProfileOverwrittenWithPutReplacesDocumentCompletely
import org.openeel.libxapi.test.assertActivityProfileWhenDeletedCannotBeRetrieved
import org.openeel.libxapi.test.assertExistingActivityProfileJsonDocumentWhenPostedMergesTopLevelProperties
import org.openeel.libxapi.test.assertMultipleActivityProfileDocumentsReturnsAllProfileIdsForActivity
import org.openeel.libxapi.test.assertMultipleActivityProfileDocumentsWithSinceReturnsOnlyNewerProfileIds
import org.openeel.libxapi.test.assertNonExistentActivityProfileReturnsNotFound
import org.openeel.libxapi.test.assertNonExistentActivityProfileWhenPostedCreatesNewDocument
import org.openeel.libxapi.test.assertNonJsonActivityProfileWhenPostedToExistingThrowsXapiException
import world.respect.datalayer.db.school.insertAdmin
import world.respect.datalayer.db.school.testSchoolDb
import world.respect.datalayer.db.school.toDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock

class XapiActivityProfileResourceDbTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun givenDocument_whenPut_thenCanBeRetrieved() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            assertActivityProfileCanBePutAndRetrieved(
                resource = dataSource.xapiResource.activityProfile
            )
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

            assertNonExistentActivityProfileReturnsNotFound(
                resource = dataSource.xapiResource.activityProfile
            )
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

            assertActivityProfileOverwrittenWithPutReplacesDocumentCompletely(
                resource = dataSource.xapiResource.activityProfile
            )
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

            assertNonExistentActivityProfileWhenPostedCreatesNewDocument(
                resource = dataSource.xapiResource.activityProfile
            )
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

            assertExistingActivityProfileJsonDocumentWhenPostedMergesTopLevelProperties(
                resource = dataSource.xapiResource.activityProfile
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

            assertNonJsonActivityProfileWhenPostedToExistingThrowsXapiException(
                resource = dataSource.xapiResource.activityProfile
            )
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

            assertActivityProfileWhenDeletedCannotBeRetrieved(
                resource = dataSource.xapiResource.activityProfile
            )
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

            assertMultipleActivityProfileDocumentsReturnsAllProfileIdsForActivity(
                resource = dataSource.xapiResource.activityProfile
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

            assertMultipleActivityProfileDocumentsWithSinceReturnsOnlyNewerProfileIds(
                resource = dataSource.xapiResource.activityProfile
            )
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
            val params = XapiActivityProfileResource.SingleDocumentParams(
                activityId = "http://example.com/activities/course-1",
                profileId = "profile-1",
            )
            val timestamp = Clock.System.now()
            val doc = XapiDocumentByteArrayImpl(
                type = "application/json",
                updated = timestamp,
                contents = """{"synced": true}""".encodeToByteArray(),
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
