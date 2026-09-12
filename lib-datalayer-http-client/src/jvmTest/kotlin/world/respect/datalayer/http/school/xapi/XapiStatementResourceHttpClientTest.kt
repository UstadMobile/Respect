package world.respect.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.assertStatementCanBeStoredAndRetrieved
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.db.RespectSchoolDatabase
//import world.respect.datalayer.db.school.newLocalSchoolDatabase
import world.respect.datalayer.http.server.XapiStatementsResourceRoute
import world.respect.datalayer.school.model.AuthToken
import world.respect.lib.test.clientservertest.insertAdminAndDefaultGrants
import world.respect.lib.test.clientservertest.newLocalSchoolDatabase
import org.openeel.libxapi.test.res.forXapiSampleStatements
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.libutil.findFreePort
import world.respect.libutil.util.time.systemTimeInMillis
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.uuid.Uuid
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationServer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient


class XapiStatementResourceHttpClientTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    val json = Json

    lateinit var server: EmbeddedServer<NettyApplicationEngine, *>

    lateinit var statementResource: XapiStatementsResource

    lateinit var serverDs: SchoolDataSourceLocal

    lateinit var serverDb: RespectSchoolDatabase

    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiationClient) {
            json(json = json)
        }
    }

    class XapiTestResource(
        val xapiResource: XapiResource
    ): XapiResource by xapiResource {

        override fun close() {
            xapiResource.close()
        }

    }

    suspend fun clientServerTest(
        block : suspend (XapiStatementsResource) -> Unit
    ) {
        val port = findFreePort()

        newLocalSchoolDatabase(
            dir = temporaryFolder.newFolder(),
            schoolUrl = Url("http://localhost:$port/"),
            localAuthenticatedUser = AuthenticatedUserPrincipalId("1"),
        ).also { (db, ds) ->
            serverDs = ds
            serverDb = db
        }

        serverDs.insertAdminAndDefaultGrants(serverDb)

        server = embeddedServer(Netty, port = port) {
            install(ContentNegotiationServer) {
                json(json = json, contentType = ContentType.Application.Json)
            }

            routing {
                XapiStatementsResourceRoute(
                    statementResource = { serverDs.xapiResource.statements },
                    json = json,
                )
            }
        }

        server.start()

        statementResource = XapiStatementsResourceHttpClient(
            xapiUrl = { Url("http://localhost:$port/") },
            httpClient = httpClient,
            tokenProvider = {
                AuthToken("secret", systemTimeInMillis(), 3600)
            },
            json = json,
        )

        try {
            block(statementResource)
        }catch(e: Throwable) {
            server.stop()

            throw e
        }
    }

    @Test
    fun givenStatement_whenConvertedToEntitiesAndBack_thenShouldMatch() {
        runBlocking {
            forXapiSampleStatements { statement ->
                clientServerTest {
                    val stmtUuid = Uuid.random()
                    val timeNow = Clock.System.now()

                    val statement = Json.decodeFromJsonElement(
                        XapiStatement.serializer(), statement.jsonObject
                    ).copy(
                        id = stmtUuid,
                        timestamp = timeNow,
                        stored = Clock.System.now(),
                    )

                    assertStatementCanBeStoredAndRetrieved(
                        statement = statement,
                        resource = statementResource
                    )
                }
            }
        }
    }
}