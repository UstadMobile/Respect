package world.respect.datalayer.repository.school.xapi

import io.ktor.server.routing.route
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.decodeFromJsonElement
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import world.respect.lib.test.clientservertest.clientServerDatasourceTest
import world.respect.lib.test.res.xapiSampleStatements
import world.respect.lib.xapi.XapiRequestHeaders
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.server.routes.school.xapi.XapiStatementsResourceRoute
import kotlin.test.Test
import kotlin.test.assertNotNull

class XapiStatementRepositoryIntegrationTest {


    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun givenStatementCreatedOnClient_whenConnected_thenWillBeStoredOnServer() {
        runBlocking {
            clientServerDatasourceTest(temporaryFolder.newFolder("test")) {
                serverRouting {
                    route("api/school/xapi") {
                        XapiStatementsResourceRoute(
                            statementResource = { serverSchoolDataSource.xapiStatementsResource },
                            json = json,
                        )
                    }
                }

                server.start()

                val client = clients.first()
                val statement: XapiStatement = xapiSampleStatements().first().let {
                    json.decodeFromJsonElement(it.jsonObject)
                }

                val response = client.schoolDataSource.xapiStatementsResource.post(
                    listOf(statement)
                )

                //TODO: change this to something event based.
                delay(1_000)

                val stmtFromServer = serverSchoolDataSource.xapiStatementsResource.get(
                    request = XapiStatementsResource.GetStatementsRequest(
                        params = XapiStatementsResource.GetStatementParams(
                            statementId = response.first(),
                        ),
                        headers = XapiRequestHeaders()
                    ),
                )

                assertNotNull(stmtFromServer)
            }
        }
    }

}