package world.respect.datalayer.repository.school

import app.cash.turbine.test
import com.eygraber.uri.Uri
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.domain.MakePlaylistOpdsFeedUseCase
import world.respect.datalayer.school.opds.ext.requireSelfUrl
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsFeedMetadata
import world.respect.lib.test.clientservertest.clientServerDatasourceTest
import world.respect.server.routes.school.respect.PlaylistRoute
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi

class PlaylistRepositoryIntegrationTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    val baseFeed = OpdsFeed(
        metadata = OpdsFeedMetadata(
            identifier = Uri.parse("urn:1234"),
            title = "Base Feed",
        ),
        /*
         Note: the database cannot currently differentiate between an empty list [] and
         a null list.
         */
        links = emptyList(),
        publications = emptyList(),
        facets = emptyList(),
        navigation = emptyList(),
        groups = emptyList(),
    )

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun givenPlaylistSavedOnServer_whenAccessedOnClient_thenLoadsAsExpected() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test")) {
                serverRouting {
                    PlaylistRoute(
                        schoolDataSource = { serverSchoolDataSource }
                    )
                }

                val playlistFeed = MakePlaylistOpdsFeedUseCase(schoolUrl = schoolUrl).invoke(
                    base = baseFeed,
                    userGuid = "test-user-guid",
                )

                serverSchoolDataSource.opdsFeedDataSource.store(
                    listOf(playlistFeed)
                )

                server.start()

                val feedFromServer = serverSchoolDataSource.opdsFeedDataSource.getByUrl(
                    url = playlistFeed.requireSelfUrl(),
                    params = DataLoadParams()
                )
                assertEquals(playlistFeed, feedFromServer.dataOrNull())

                assertEquals(
                    expected = playlistFeed,
                    actual = clients.first().schoolDataSource.opdsFeedDataSource.getByUrl(
                        url = playlistFeed.requireSelfUrl(),
                        params = DataLoadParams()
                    ).dataOrNull(),
                )
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun givenPlaylistCreatedOnClient_whenUploadedOnServer_thenLoadsAsExpected() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test")) {
                serverRouting {
                    PlaylistRoute(
                        schoolDataSource = { serverSchoolDataSource }
                    )
                }

                server.start()

                val playlistFeed = MakePlaylistOpdsFeedUseCase(schoolUrl = schoolUrl).invoke(
                    base = baseFeed,
                    userGuid = "test-user-guid",
                )
                clients.first().schoolDataSource.opdsFeedDataSource.store(
                    listOf(playlistFeed)
                )

                serverSchoolDataSource.opdsFeedDataSource.getByUrlAsFlow(
                    url = playlistFeed.requireSelfUrl(),
                    params = DataLoadParams()
                ).filter {
                    it.dataOrNull() != null
                }.test(timeout = (10 * 1000).seconds) {
                    assertEquals(
                        expected = playlistFeed,
                        actual = awaitItem().dataOrNull()
                    )

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }





}