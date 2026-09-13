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
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiActivityProfileResourceTest
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.http.server.XapiActivityProfileResourceRoute
import world.respect.datalayer.school.model.AuthToken
import world.respect.lib.test.clientservertest.insertAdminAndDefaultGrants
import world.respect.lib.test.clientservertest.newLocalSchoolDatabase
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.libutil.findFreePort
import world.respect.libutil.util.time.systemTimeInMillis
import kotlin.test.Test
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationServer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient

class XapiActivityProfileResourceHttpClientTest : AbstractXapiActivityProfileResourceTest() {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    lateinit var server: EmbeddedServer<NettyApplicationEngine, *>

    lateinit var activityProfileResource: XapiActivityProfileResource

    lateinit var serverDs: SchoolDataSourceLocal

    lateinit var serverDb: RespectSchoolDatabase

    val json = Json

    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiationClient) {
            json(json = json)
        }
    }

    override suspend fun withXapiActivityProfileResource(block: suspend (XapiActivityProfileResource) -> Unit) {
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
                route("activities") {
                    XapiActivityProfileResourceRoute(
                        activityProfileResource = { serverDs.xapiResource.activityProfile }
                    )
                }
            }
        }

        server.start()

        activityProfileResource = XapiActivityProfileResourceHttpClient(
            xapiUrl = { Url("http://localhost:$port/") },
            httpClient = httpClient,
            tokenProvider = {
                AuthToken("secret", systemTimeInMillis(), 3600)
            },
        )

        try {
            block(activityProfileResource)
        } finally {
            server.stop()
        }
    }

    @Test
    fun test() {
        givenMultipleDocumentsWithTimestamps_whenGetMultipleDocumentsWithSince_thenReturnsOnlyNewerProfileIds()
    }
}
