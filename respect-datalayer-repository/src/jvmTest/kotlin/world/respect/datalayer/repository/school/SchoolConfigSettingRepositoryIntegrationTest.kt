package world.respect.datalayer.repository.school

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
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.SchoolConfigSetting
import world.respect.datalayer.shared.params.GetListCommonParams
import world.respect.lib.test.clientservertest.clientServerDatasourceTest
import world.respect.server.routes.school.respect.SchoolConfigSettingRoute
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class SchoolConfigSettingRepositoryIntegrationTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @BeforeTest
    fun setup() {
        Napier.base(DebugAntilog())
    }

    private fun getTestSetting(key: String = "test-key", value: String = "test-value") =
        SchoolConfigSetting(
            key = key,
            value = value,
            canRead = listOf(PersonRoleEnum.SYSTEM_ADMINISTRATOR),
            canWrite = listOf(PersonRoleEnum.SYSTEM_ADMINISTRATOR)
        )

    @Test
    fun givenRequestMade_whenSameRequestMadeAgain_thenRemoteDataWillReturnNotModified() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-not-mod")) {
                serverRouting {
                    route("api/school/respect") {
                        SchoolConfigSettingRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                clients.first().insertServerAdminAndDefaultGrants()

                val setting = getTestSetting()

                serverSchoolDataSource.schoolConfigSettingDataSource.store(listOf(setting))

                val params = SchoolConfigSettingDataSource.GetListParams(keys = listOf(setting.key))

                val initData = clients.first().schoolDataSource.schoolConfigSettingDataSource.list(
                    loadParams = DataLoadParams(),
                    params = params
                )

                val validatedData =
                    clients.first().schoolDataSource.schoolConfigSettingDataSource.list(
                        loadParams = DataLoadParams(),
                        params = params
                    )

                assertTrue(initData.dataOrNull()!!.any { it.key == setting.key })
                assertEquals(
                    NoDataLoadedState.Reason.NOT_MODIFIED,
                    (validatedData.remoteState as? NoDataLoadedState)?.reason
                )
            }
        }
    }

    @Test
    fun givenRequestMade_whenDataChangedAndSameRequestMadeAgain_thenRemoteDataWillBeLoaded() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-remote-load")) {
                serverRouting {
                    route("api/school/respect") {
                        SchoolConfigSettingRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                clients.first().insertServerAdminAndDefaultGrants()

                val setting = getTestSetting()
                serverSchoolDataSource.schoolConfigSettingDataSource.store(listOf(setting))


                val params = SchoolConfigSettingDataSource.GetListParams(keys = listOf(setting.key))
                val initData = clients.first().schoolDataSource.schoolConfigSettingDataSource.list(
                    loadParams = DataLoadParams(),
                    params = params
                )

                val updatedValue = "updated"
                //DataSource will need to reject same-second changes and respond with a wait message.
                Thread.sleep(2_000)

                serverSchoolDataSource.schoolConfigSettingDataSource.store(
                    listOf(
                        setting.copy(
                            value = updatedValue,
                            lastModified = Clock.System.now(),
                        )
                    )
                )

                val newData = clients.first().schoolDataSource.schoolConfigSettingDataSource.list(
                    loadParams = DataLoadParams(),
                    params = params
                )

                assertTrue(initData.dataOrNull()!!.any { it.key == setting.key })
                assertTrue(newData.remoteState is DataReadyState)
                assertEquals(
                    updatedValue,
                    newData.dataOrNull()!!.first { it.key == setting.key }.value
                )
            }
        }
    }

    @Test
    fun givenRequestMade_whenNextRequestSinceParamSetToPreviousConsistentThroughValue_thenRemoteResultShouldBeEmpty() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-since-empty")) {
                serverRouting {
                    route("api/school/respect") {
                        SchoolConfigSettingRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                val client = clients.first()
                client.insertServerAdminAndDefaultGrants()

                val setting = getTestSetting()
                serverSchoolDataSource.schoolConfigSettingDataSource.store(listOf(setting))

                val startTime = Clock.System.now()
                val params = SchoolConfigSettingDataSource.GetListParams(keys = listOf(setting.key))
                val initData = client.schoolDataSource.schoolConfigSettingDataSource.list(
                    loadParams = DataLoadParams(),
                    params = params
                )

                val answer1ConsistentThrough = initData.remoteState?.metaInfo?.consistentThrough!!
                assertTrue(answer1ConsistentThrough >= startTime)

                val dataSince = client.schoolDataSource.schoolConfigSettingDataSource.list(
                    loadParams = DataLoadParams(),
                    params = SchoolConfigSettingDataSource.GetListParams(
                        keys = listOf(setting.key),
                        common = GetListCommonParams(
                            since = answer1ConsistentThrough
                        )
                    )
                )

                val remoteDataState = dataSince.remoteState
                assertTrue(remoteDataState is DataReadyState)
                val remoteData = remoteDataState.data as List<*>
                assertEquals(0, remoteData.size)
            }
        }
    }

    @Test
    fun givenRequestMade_whenDataChangedAndNextRequestSinceParamSetToPreviousConsistentThroughValue_thenRemoteResultShouldBeUpdated() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-since-updated")) {
                serverRouting {
                    route("api/school/respect") {
                        SchoolConfigSettingRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                val client = clients.first()
                client.insertServerAdminAndDefaultGrants()

                val setting = getTestSetting()
                serverSchoolDataSource.schoolConfigSettingDataSource.store(listOf(setting))

                val startTime = Clock.System.now()
                val params = SchoolConfigSettingDataSource.GetListParams(keys = listOf(setting.key))
                val initData = client.schoolDataSource.schoolConfigSettingDataSource.list(
                    loadParams = DataLoadParams(),
                    params = params
                )

                val answer1ConsistentThrough = initData.remoteState?.metaInfo?.consistentThrough!!
                assertTrue(answer1ConsistentThrough >= startTime)

                val updatedValue = "updated"
                serverSchoolDataSource.schoolConfigSettingDataSource.store(
                    listOf(
                        setting.copy(
                            value = updatedValue,
                            lastModified = Clock.System.now()
                        )
                    )
                )

                val dataSince = client.schoolDataSource.schoolConfigSettingDataSource.list(
                    loadParams = DataLoadParams(),
                    params = SchoolConfigSettingDataSource.GetListParams(
                        keys = listOf(setting.key),
                        common = GetListCommonParams(
                            since = answer1ConsistentThrough
                        )
                    )
                )

                val remoteDataState = dataSince.remoteState
                assertTrue(remoteDataState is DataReadyState)
                @Suppress("UNCHECKED_CAST")
                val remoteData = remoteDataState.data as List<SchoolConfigSetting>
                assertEquals(1, remoteData.size)
                assertEquals(updatedValue, remoteData.first().value)
            }
        }
    }

    @Test
    fun givenSettingWrittenLocally_whenStored_thenWillSendToRemote() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-sync")) {
                serverRouting {
                    route("api/school/respect") {
                        SchoolConfigSettingRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                val client = clients.first()
                client.insertServerAdminAndDefaultGrants()

                val setting = getTestSetting("sync-key", "sync-value")

                client.schoolDataSource.schoolConfigSettingDataSource.store(
                    listOf(setting)
                )

                serverSchoolDataSource.schoolConfigSettingDataSource.listAsFlow(
                    params = SchoolConfigSettingDataSource.GetListParams(keys = listOf(setting.key))
                ).filter {
                    it is DataReadyState && it.data.any { s -> s.key == setting.key }
                }.test(timeout = 30.seconds) {
                    val item = awaitItem()
                    assertEquals(
                        "sync-value",
                        item.dataOrNull()?.first { it.key == setting.key }?.value
                    )
                }
            }
        }
    }

    @Test
    fun givenFindByGuid_whenDataNotInLocalCache_thenFetchesFromRemote() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-find-by-guid")) {
                serverRouting {
                    route("api/school/respect") {
                        SchoolConfigSettingRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                val client = clients.first()
                client.insertServerAdminAndDefaultGrants()

                val setting = getTestSetting("guid-test-key", "guid-test-value")
                serverSchoolDataSource.schoolConfigSettingDataSource.store(listOf(setting))

                // This should fetch from remote since not in local cache
                val result = client.schoolDataSource.schoolConfigSettingDataSource.findByGuid(
                    params = DataLoadParams(),
                    guid = setting.key
                )

                assertTrue(result is DataReadyState)
                assertEquals(setting.value, result.data.value)
            }
        }
    }

    @Test
    fun givenFindByGuid_whenDataInLocalCacheAndOnlyIfCachedTrue_thenDoesNotHitRemote() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-find-by-guid-cached")) {
                serverRouting {
                    route("api/school/respect") {
                        SchoolConfigSettingRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                val client = clients.first()
                client.insertServerAdminAndDefaultGrants()

                val setting = getTestSetting("guid-cached-key", "guid-cached-value")
                serverSchoolDataSource.schoolConfigSettingDataSource.store(listOf(setting))

                // First request - populates cache
                client.schoolDataSource.schoolConfigSettingDataSource.findByGuid(
                    params = DataLoadParams(),
                    guid = setting.key
                )

                // Second request with onlyIfCached = true - should not hit remote
                val cachedResult = client.schoolDataSource.schoolConfigSettingDataSource.findByGuid(
                    params = DataLoadParams(onlyIfCached = true),
                    guid = setting.key
                )

                assertTrue(cachedResult is DataReadyState)
                assertEquals(setting.value, cachedResult.data.value)
            }
        }
    }

    @Test
    fun givenListAsFlow_whenDataChanges_thenFlowEmitsUpdates() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test-flow")) {
                serverRouting {
                    route("api/school/respect") {
                        SchoolConfigSettingRoute(schoolDataSource = { serverSchoolDataSource })
                    }
                }

                server.start()

                val client = clients.first()
                client.insertServerAdminAndDefaultGrants()

                val setting = getTestSetting("flow-key", "flow-value")

                client.schoolDataSource.schoolConfigSettingDataSource.listAsFlow(
                    loadParams = DataLoadParams(),
                    params = SchoolConfigSettingDataSource.GetListParams(keys = listOf(setting.key))
                ).test(timeout = 30.seconds) {
                    // Initially no data
                    val initial = awaitItem()
                    assertTrue(initial is DataReadyState && initial.data.isEmpty())

                    // Store data
                    client.schoolDataSource.schoolConfigSettingDataSource.store(listOf(setting))

                    // Should receive update with the data
                    val updated = awaitItem()
                    assertTrue(updated is DataReadyState)
                    assertEquals(
                        "flow-value",
                        updated.dataOrNull()?.first { it.key == setting.key }?.value
                    )
                }
            }
        }
    }
}
