package world.respect.datalayer.db.school.xapi

import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiActivityProfileResourceTest
import world.respect.datalayer.db.school.insertAdmin
import world.respect.datalayer.db.school.testSchoolDb
import world.respect.datalayer.db.school.toDataSource
import world.respect.lib.dataloadstate.datetime.roundToEpochSeconds
import world.respect.lib.dataloadstate.datetime.toGMTDate
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock

class XapiActivityProfileResourceDbTest : AbstractXapiActivityProfileResourceTest() {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    override suspend fun withXapiActivityProfileResource(
        block: suspend (XapiActivityProfileResource) -> Unit
    ) {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            block(dataSource.xapiResource.activityProfile)
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
            val timestamp = Clock.System.now().roundToEpochSeconds()
            val doc = XapiDocumentByteArrayImpl(
                type = "application/json",
                updated = timestamp.toGMTDate(),
                contents = """{"synced": true}""".encodeToByteArray(),
            )

            resource.updateLocal(params, doc)

            val retrieved = resource.get(params).dataOrNull()
            assertNotNull(retrieved)
            assertEquals("""{"synced": true}""", retrieved.contentsAsByteArray().decodeToString())
            assertEquals("application/json", retrieved.type)
            assertEquals(timestamp.toGMTDate(), retrieved.updated)
        }
    }
}
