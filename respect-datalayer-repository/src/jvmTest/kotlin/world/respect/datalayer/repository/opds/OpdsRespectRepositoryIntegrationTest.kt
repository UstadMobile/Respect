package world.respect.datalayer.repository.opds

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.opds.OpdsDataSourceDb
import world.respect.datalayer.http.opds.OpdsDataSourceHttp
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libutil.findFreePort
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class OpdsRespectRepositoryIntegrationTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    data class OpdsRepositoryIntegrationTestContext(
        val port: Int,
        val db: RespectSchoolDatabase,
        val json: Json,
        val okHttpClient: OkHttpClient,
        val httpClient: HttpClient,
        val xxStringHasher: XXStringHasherCommonJvm,
        val local: OpdsDataSourceDb,
        val remote: OpdsDataSourceHttp,
        val repository: OpdsDataSourceRepository
    )

    private fun opdsIntegrationTest(
        block: OpdsRepositoryIntegrationTestContext.() -> Unit
    ) {
        val port = findFreePort()
        println("port = $port")
        val server = embeddedServer(Netty, port = port) {
            install(ConditionalHeaders)
            install(CallLogging)

            routing {
                staticResources("/resources", "/world/respect/datalayer/repository/opds")
            }
        }.start()

        try {
            val dbFile = temporaryFolder.newFile("respect-school.db")
            val db = Room.databaseBuilder<RespectSchoolDatabase>(dbFile.absolutePath)
                .setDriver(BundledSQLiteDriver())
                .build()

            val json = Json { ignoreUnknownKeys = true }

            val okHttpClient = OkHttpClient.Builder().build()

            val httpClient = HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(json = json)
                }
                engine {
                    preconfigured = okHttpClient
                }
            }

            val xxStringHasher = XXStringHasherCommonJvm()

            val localDataSource = OpdsDataSourceDb(
                respectSchoolDatabase = db,
                json = json,
                uidNumberMapper = XXHashUidNumberMapper(xxStringHasher),
                primaryKeyGenerator = PrimaryKeyGenerator(RespectSchoolDatabase.TABLE_IDS)
            )

            val httpDataSource = OpdsDataSourceHttp(
                httpClient, localDataSource.feedNetworkValidationHelper
            )

            val repository = OpdsDataSourceRepository(localDataSource, httpDataSource)
            block(
                OpdsRepositoryIntegrationTestContext(
                    port = port,
                    db = db,
                    json = json,
                    okHttpClient = okHttpClient,
                    httpClient = httpClient,
                    xxStringHasher = xxStringHasher,
                    local = localDataSource,
                    remote = httpDataSource,
                    repository = repository
                )
            )
        }finally {
            server.stop()
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun givenOpdsFeed_whenLoadOpdsFeed_thenFlowWillLoadIt() {
        opdsIntegrationTest {
            val url = Url("http://localhost:$port/resources/index.json")
            runBlocking {
                val loadStart = Clock.System.now().toEpochMilliseconds()
                repository.loadOpdsFeed(url, DataLoadParams()).filter {
                    it is DataReadyState
                }.test(timeout = 10.seconds) {
                    val data = awaitItem()
                    val waitTime = Clock.System.now().toEpochMilliseconds() - loadStart
                    println("Loaded in $waitTime ms")
                    assertEquals("Main Menu", data.dataOrNull()?.metadata?.title)
                    cancelAndIgnoreRemainingEvents()
                }

                repository.loadOpdsFeed(url, DataLoadParams()).filter {
                    it is DataReadyState && it.remoteState is NoDataLoadedState
                }.test(timeout = 10.seconds) {
                    val data = awaitItem()
                    val waitTime = Clock.System.now().toEpochMilliseconds() - loadStart
                    println("Loaded in $waitTime ms")
                    assertEquals("Main Menu", data.dataOrNull()?.metadata?.title)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun givenOpdsFeedLoaded_whenLoadedAgain_thenDataIsLoadedAndNetworkResponseIsNotModified() {
        opdsIntegrationTest {
            val url = Url("http://localhost:$port/resources/index.json")
            runBlocking {
                withTimeout(15_000) {
                    repository.loadOpdsFeed(url, DataLoadParams()).filter {
                        it is DataReadyState
                    }.first()

                    repository.loadOpdsFeed(url, DataLoadParams()).filter {
                        it is DataReadyState && it.remoteState is NoDataLoadedState
                    }.test(timeout = 10.seconds) {
                        val data = awaitItem()
                        assertEquals(NoDataLoadedState.Reason.NOT_MODIFIED,
                            (data.remoteState as? NoDataLoadedState)?.reason)

                        assertEquals("Main Menu", data.dataOrNull()?.metadata?.title)
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
    }

    /*
    @OptIn(ExperimentalTime::class)
    @Test
    fun givenOpdsPublication_whenLoadedThenWillEmitFlow() {
        opdsIntegrationTest {
            val url = Url("http://localhost:$port/resources/lesson001.json")
            runBlocking {
                val loadStart = Clock.System.now().toEpochMilliseconds()
                repository.loadOpdsPublication(
                    url, DataLoadParams(), null, null
                ).filter {
                    it is DataReadyState
                }.test(timeout = 10.seconds) {
                    val data = awaitItem()
                    val waitTime = Clock.System.now().toEpochMilliseconds() - loadStart
                    println("Loaded in $waitTime ms")

                    assertEquals(LangMapStringValue("Lesson 001"),
                        data.dataOrNull()?.metadata?.title)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }*/


}