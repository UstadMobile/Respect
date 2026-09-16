package world.respect.datalayer.repository.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.Routing
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.writequeue.RemoteWriteQueueDbImpl
import world.respect.datalayer.http.school.xapi.XapiResourceHttpClient
import world.respect.datalayer.repository.util.mkdirsIfNotExists
import world.respect.datalayer.school.model.AuthToken
import world.respect.lib.test.clientservertest.EmbeddedDataSourceServerContext
import world.respect.lib.test.clientservertest.newLocalSchoolDatabase
import world.respect.lib.test.clientservertest.withEmbeddedDataSourceServer
import world.respect.libutil.util.time.systemTimeInMillis
import java.io.File
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient

data class RepositoryTestClient(
    val dir: File,
    val schoolDb: RespectSchoolDatabase,
    val datasource: XapiResourceRepository,
)

data class RepositoryTestContext(
    val serverContext: EmbeddedDataSourceServerContext,
    val clients: List<RepositoryTestClient>,
)

suspend fun withEmbeddedServerAndRepositoryResources(
    workDir: File,
    start: Boolean = true,
    routingConfig: Routing.(EmbeddedDataSourceServerContext) -> Unit,
    numClients: Int = 1,
    localAuthenticatedUser: AuthenticatedUserPrincipalId = AuthenticatedUserPrincipalId("1"),
    json: Json = Json,
    httpClient: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiationClient) {
            json(json = json)
        }
    },
    block: suspend RepositoryTestContext.() -> Unit
) {
    withEmbeddedDataSourceServer(
        dbDir = File(workDir, "server").mkdirsIfNotExists(),
        routingConfig = routingConfig,
        start = start,
    ) {
        val schoolUrlVal = this.schoolUrl
        val serverContext = this

        val clients = (1 .. numClients).map { index ->
            val clientDir = File(workDir, "client-$index").mkdirsIfNotExists()
            val (schoolDb, schoolLocalDs) = newLocalSchoolDatabase(
                dir = clientDir,
                schoolUrl = schoolUrlVal,
                localAuthenticatedUser = localAuthenticatedUser,
            )

            val xapiResourceHttpClient = XapiResourceHttpClient(
                xapiUrl = { schoolUrlVal },
                httpClient = httpClient,
                tokenProvider = {
                    AuthToken("secret", systemTimeInMillis(), 3600)
                },
                json = json,
            )

            val enqueueDrainRemoteChannel = EnqueueDrainRemoteWriteQueueUseCaseChannel()

            val remoteWriteQueue = RemoteWriteQueueDbImpl(
                schoolDb = schoolDb,
                account = localAuthenticatedUser,
                enqueueDrainRemoteWriteQueueUseCase = enqueueDrainRemoteChannel,
            )


            RepositoryTestClient(
                dir = clientDir,
                schoolDb = schoolDb,
                datasource = XapiResourceRepository(
                    local = schoolLocalDs.xapiResource,
                    remote = xapiResourceHttpClient,
                    remoteWriteQueue = remoteWriteQueue,
                    json = json,
                )
            )
        }

        block(
            RepositoryTestContext(
                serverContext = serverContext,
                clients = clients,
            )
        )
    }

}