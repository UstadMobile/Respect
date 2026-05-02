package world.respect.datalayer.repository.school.xapi

import app.cash.turbine.test
import io.ktor.server.routing.route
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

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


                val stmtUuid = statement.id!!

                client.schoolDataSource.xapiStatementsResource.post(
                    listOf(statement)
                )

                serverDb.invalidationTracker.createFlow(
                    "XapiStatementEntity"
                ).map {
                    serverSchoolDataSource.xapiStatementsResource.get(
                        request = XapiStatementsResource.GetStatementsRequest(
                            params = XapiStatementsResource.GetStatementParams(
                                statementId = stmtUuid,
                            ),
                            headers = XapiRequestHeaders()
                        ),
                    )
                }.filter {
                    it.statementResult.statements.firstOrNull()?.id == stmtUuid
                }.test(timeout = 5.seconds) {
                    val stmtFromServer = awaitItem().statementResult.statements.firstOrNull()
                    assertNotNull(stmtFromServer)
                    assertEquals(stmtUuid, stmtFromServer.id)
                }
            }
        }
    }

    @Test
    fun givenStatementOnServer_whenGetOnRepoCalled_thenWillBeFetched() {
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

                val stmtUuid = statement.id!!

                serverSchoolDataSource.xapiStatementsResource.post(
                    listOf(statement)
                )

                val stmtFromClient = client.schoolDataSource.xapiStatementsResource.get(
                    request = XapiStatementsResource.GetStatementsRequest(
                        params = XapiStatementsResource.GetStatementParams(
                            statementId = stmtUuid,
                        ),
                        headers = XapiRequestHeaders()
                    ),
                ).statementResult.statements.firstOrNull()

                assertNotNull(stmtFromClient)
                assertEquals(stmtUuid, stmtFromClient.id)
            }
        }
    }


}