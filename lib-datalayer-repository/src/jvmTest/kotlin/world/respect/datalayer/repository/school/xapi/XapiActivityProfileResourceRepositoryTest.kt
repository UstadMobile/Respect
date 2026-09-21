package world.respect.datalayer.repository.school.xapi

import app.cash.turbine.test
import io.ktor.server.routing.route
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiActivityProfileResourceTest
import org.openeel.libxapi.test.XapiActivityProfileTestParams
import world.respect.datalayer.http.server.XapiActivityProfileResourceRoute
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds


class XapiActivityProfileResourceRepositoryTest: AbstractXapiActivityProfileResourceTest() {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    override suspend fun withXapiDocumentResource(
        block: suspend (XapiActivityProfileResource) -> Unit
    ) {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
            routingConfig = { serverContext ->
                XapiActivityProfileResourceRoute(
                    activityProfileResource = {
                        serverContext.datasourceContext.datasource.xapiResource.activityProfile
                    }
                )
            }
        ) {
            block(clients.first().datasource.activityProfile)
        }
    }

    @Test
    fun givenDocumentPostedOnClient_whenGetFlowOnServerCollected_thenIsCollected() = runBlocking {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
            routingConfig = { serverContext ->
                route("activities") {
                    XapiActivityProfileResourceRoute(
                        activityProfileResource = {
                            serverContext.datasourceContext.datasource.xapiResource.activityProfile
                        }
                    )
                }
            }
        ) {
            val params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1

            clients.first().datasource.activityProfile.post(
                params = params,
                document = XapiActivityProfileTestParams.DOC
            )

            serverContext.datasourceContext.datasource.xapiResource.activityProfile.getAsFlow(
                params = params,
                dataLoadParams = DataLoadParams()
            ).mapNotNull {
                it as? DataReadyState
            }.test(timeout = 5.seconds) {
                assertNotNull(awaitItem().data)
            }
        }
    }

    @Test
    fun givenDocumentedPostedOnServer_whenGetCalledOnClient_thenMatches() = runBlocking {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
            routingConfig = { serverContext ->
                route("activities") {
                    XapiActivityProfileResourceRoute(
                        activityProfileResource = {
                            serverContext.datasourceContext.datasource.xapiResource.activityProfile
                        }
                    )
                }
            }
        ) {
            val params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1

            serverContext.datasourceContext.datasource.xapiResource.activityProfile.post(
                params = params, document = XapiActivityProfileTestParams.DOC
            )

            val clientDocLoadState = clients.first().datasource.activityProfile.get(params = params)
            assertIs<DataReadyState<XapiDocument>>(clientDocLoadState)
        }
    }

    @Test
    fun givenDocumentPostedOnServer_whenGetAsFlowCalledOnClient_thenFlowReceivesMatchingData() = runBlocking {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
            routingConfig = { serverContext ->
                route("activities") {
                    XapiActivityProfileResourceRoute(
                        activityProfileResource = {
                            serverContext.datasourceContext.datasource.xapiResource.activityProfile
                        }
                    )
                }
            }
        ) {
            val params = XapiActivityProfileTestParams.SINGLE_DOC_PARAMS1

            serverContext.datasourceContext.datasource.xapiResource.activityProfile.post(
                params = params, document = XapiActivityProfileTestParams.DOC
            )

            clients.first().datasource.activityProfile.getAsFlow(
                params = params,
                dataLoadParams = DataLoadParams()
            ).filterIsInstance<DataReadyState<XapiDocument>>().test(
                timeout = 5.seconds
            ) {
                assertContentEquals(
                    expected = XapiActivityProfileTestParams.DOC.contentsAsByteArray(),
                    actual = awaitItem().data.contentsAsByteArray()
                )
            }
        }
    }


}