package world.respect.datalayer.repository.school.xapi

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiActivityProfileResourceTest
import world.respect.datalayer.http.server.XapiActivityProfileResourceRoute
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import kotlin.test.Test

/*
class XapiActivityProfileResourceRepositoryTest: AbstractXapiActivityProfileResourceTest() {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    override suspend fun withXapiActivityProfileResource(
        block: suspend (XapiActivityProfileResource) -> Unit
    ) {
        withEmbeddedServerAndRepositoryResources(
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
    fun test() {
        givenNonExistentDocument_whenPosted_thenCreatesNewDocument()
    }

    @Test
    fun test2() {
        givenExistingJsonDocument_whenPosted_thenMergesTopLevelProperties()
    }

}*/
