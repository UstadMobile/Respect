package world.respect.datalayer.repository.school

import androidx.paging.PagingSource
import app.cash.turbine.test
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.server.routing.route
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.repository.clientservertest.clientServerDatasourceTest
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.Person
import world.respect.libutil.util.time.systemTimeInMillis
import world.respect.server.routes.school.respect.PersonRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * This integration test checks if using consistent-through and since parameters works with a real
 * datasource as expected.
 */
class PersonRepositoryIntegrationTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    val defaultTestPerson = Person(
        guid = "42",
        username = "test",
        givenName = "test",
        familyName = "test",
        roles = emptyList(),
    )

    @Test
    fun givenRequestMade_whenSameRequestMadeAgain_thenRemoteDataWillReturnNotModified() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test")) {
                serverRouting {
                    route("api/school/respect") {
                        PersonRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                serverSchoolDataSource.personDataSource.store(
                    listOf(defaultTestPerson)
                )

                val initData = clients.first().schoolDataSource.personDataSource
                    .list(DataLoadParams(), null)

                val validatedData = clients.first().schoolDataSource.personDataSource
                    .list(DataLoadParams(), null)

                assertEquals(
                    defaultTestPerson.guid,
                    initData.dataOrNull()!!.first().guid
                )

                assertEquals(
                    NoDataLoadedState.Reason.NOT_MODIFIED,
                    (validatedData.remoteState as? NoDataLoadedState)?.reason
                )

                assertEquals(
                    defaultTestPerson.guid,
                    validatedData.dataOrNull()!!.first().guid
                )
            }
        }
    }

    @Test
    fun givenRequestMade_whenDataChangedAndSameRequestMadeAgain_thenRemoteDataWillBeLoaded() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test")) {
                serverRouting {
                    route("api/school/respect") {
                        PersonRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                serverSchoolDataSource.personDataSource.store(
                    listOf(defaultTestPerson)
                )

                val initData = clients.first().schoolDataSource.personDataSource
                    .list(DataLoadParams(), null)

                val updatedName = "updated"
                //DataSource will need to reject same-second changes and respond with a wait message.
                Thread.sleep(2_000)

                serverSchoolDataSource.personDataSource.store(
                    listOf(
                        defaultTestPerson.copy(
                            givenName = updatedName,
                            lastModified = Clock.System.now(),
                        )
                    )
                )

                val newData = clients.first().schoolDataSource.personDataSource
                    .list(DataLoadParams(), null)

                assertEquals(
                    defaultTestPerson.guid,
                    initData.dataOrNull()!!.first().guid
                )

                assertTrue(newData.remoteState is DataReadyState)

                assertEquals(
                    updatedName,
                    newData.dataOrNull()!!.first().givenName
                )
            }
        }
    }


    /**
     * Test that once a request is made, the next request will use the since parameter.
     */
    @Test
    fun givenRequestMade_whenNextRequestSinceParamSetToPreviousConsistentThroughValue_thenRemoteResultShouldBeEmpty() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test")) {
                serverRouting {
                    route("api/school/respect") {
                        PersonRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                serverSchoolDataSource.personDataSource.store(
                    listOf(defaultTestPerson)
                )

                val startTime = systemTimeInMillis()
                val initData = clients.first().schoolDataSource.personDataSource
                    .list(DataLoadParams(), null)
                println(initData)
                val answer1ConsistentThrough = initData.remoteState?.metaInfo?.consistentThrough!!
                assertTrue(initData.remoteState?.metaInfo?.consistentThrough!! >= startTime)

                val dataSince = clients.first().schoolDataSource.personDataSource
                    .list(
                        loadParams = DataLoadParams(),
                        since = Instant.Companion.fromEpochMilliseconds(answer1ConsistentThrough)
                    )

                val remoteDataState = dataSince.remoteState
                assertTrue(remoteDataState is DataReadyState)
                val remoteData = remoteDataState.data as List<*>
                assertEquals(0, remoteData.size)

                println("Run time: ${systemTimeInMillis() - startTime}")
            }
        }
    }

    @Test
    fun givenRequestMade_whenDataChangedAndNextRequestSinceParamSetToPreviousConsistentThroughValue_thenRemoteResultShouldBeUpdated() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test")) {
                serverRouting {
                    route("api/school/respect") {
                        PersonRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                serverSchoolDataSource.personDataSource.store(
                    listOf(defaultTestPerson)
                )

                val startTime = systemTimeInMillis()
                val initData = clients.first().schoolDataSource.personDataSource
                    .list(DataLoadParams(), null)
                println(initData)
                val answer1ConsistentThrough = initData.remoteState?.metaInfo?.consistentThrough!!
                assertTrue(initData.remoteState?.metaInfo?.consistentThrough!! >= startTime)

                val updatedName = "updated"
                serverSchoolDataSource.personDataSource.store(
                    listOf(
                        defaultTestPerson.copy(
                            givenName = "updated",
                            lastModified = Clock.System.now()
                        )
                    )
                )

                val dataSince = clients.first().schoolDataSource.personDataSource
                    .list(
                        loadParams = DataLoadParams(),
                        since = Instant.Companion.fromEpochMilliseconds(answer1ConsistentThrough)
                    )

                val remoteDataState = dataSince.remoteState
                assertTrue(remoteDataState is DataReadyState)
                @Suppress("UNCHECKED_CAST")
                val remoteData = remoteDataState.data as List<Person>
                assertEquals(1, remoteData.size)
                assertEquals(updatedName, remoteData.first().givenName)

                println("Run time: ${systemTimeInMillis() - startTime}")
            }
        }
    }

    @Test
    fun givenRemotePagingSourceWillLoad() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test")) {
                serverRouting {
                    route("api/school/respect") {
                        PersonRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                serverSchoolDataSource.personDataSource.store(
                    listOf(defaultTestPerson)
                )

                val pagingSource = clients.first().schoolDataSource.personDataSource
                    .listAsPagingSource(DataLoadParams(), PersonDataSource.GetListParams())

                pagingSource.load(
                    PagingSource.LoadParams.Refresh(0, 50, false)
                )

                clients.first().schoolDataSource.personDataSource.listAsFlow(
                    DataLoadParams()
                ).filter { it is DataReadyState && it.data.isNotEmpty() }.test(
                    timeout = 10.seconds
                ) {
                    val localData = awaitItem()
                    assertEquals(1, localData.dataOrNull()?.size)
                    assertEquals(defaultTestPerson.givenName,
                        localData.dataOrNull()?.first()?.givenName)
                }
            }
        }
    }

    @Test
    fun givenPersonWrittenLocally_whenStored_thenWillSendToRemote() {
        Napier.base(DebugAntilog())
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test")) {
                serverRouting {
                    route("api/school/respect") {
                        PersonRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                clients.first().schoolDataSource.personDataSource.store(
                    listOf(defaultTestPerson)
                )

                serverSchoolDataSource.personDataSource.findByGuidAsFlow(
                    defaultTestPerson.guid
                ).filter {
                    it is DataReadyState
                }.test(timeout = 30.seconds) {
                    val item = awaitItem()
                    assertEquals(defaultTestPerson.guid, item.dataOrNull()?.guid)
                }
            }
        }
    }

}