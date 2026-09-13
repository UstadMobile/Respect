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
import org.openeel.libxapi.test.AbstractXapiStatementResourceTest
import org.openeel.libxapi.test.assertStatementCanBeStoredAndRetrieved
import org.openeel.libxapi.test.res.SampleXapiStatement
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.db.RespectSchoolDatabase
//import world.respect.datalayer.db.school.newLocalSchoolDatabase
import world.respect.datalayer.http.server.XapiStatementsResourceRoute
import world.respect.datalayer.school.model.AuthToken
import world.respect.lib.test.clientservertest.insertAdminAndDefaultGrants
import world.respect.lib.test.clientservertest.newLocalSchoolDatabase
import org.openeel.libxapi.test.res.forXapiSampleStatements
import org.openeel.libxapi.test.res.xapiSampleStatements
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


class XapiStatementResourceHttpClientTest: AbstractXapiStatementResourceTest() {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiationClient) {
            json(json = json)
        }
    }


    override fun loadXapiSampleStatements(): List<SampleXapiStatement> = xapiSampleStatements()

    override suspend fun withXapiStatementResource(block: suspend (XapiStatementsResource) -> Unit) {
        withEmbeddedDataSourceServer(
            dbDir = temporaryFolder.newFolder(),
            routingConfig = { context ->
                XapiStatementsResourceRoute(
                    statementResource = {
                        context.datasourceContext.datasource.xapiResource.statements
                    },
                    json = json,
                )
            }
        ) {
            val statementResource = XapiStatementsResourceHttpClient(
                xapiUrl = { schoolUrl },
                httpClient = httpClient,
                tokenProvider = {
                    AuthToken("secret", systemTimeInMillis(), 3600)
                },
                json = json,
            )

            block(statementResource)
        }
    }

    /**
     * This function exists just to tell Android Studio/IntelliJ that this is a test class,
     * without at least one function annotated test it won't show the run test option
     */
    @Test
    fun thisIsATestClass() {}

}