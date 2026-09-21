package world.respect.datalayer.repository.school.xapi

import kotlinx.serialization.json.Json
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import world.respect.lib.xapi.resources.local.XapiResourceLocal
import world.respect.lib.xapi.resources.XapiActivitiesResource
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.XapiAgentsResource
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStateResource
import world.respect.lib.xapi.resources.XapiStatementsResource

class XapiResourceRepository(
    private val local: XapiResourceLocal,
    private val remote: XapiResource,
    private val remoteWriteQueue: XapiRemoteWriteQueue,
    private val json: Json,
) : XapiResource {


    override val statements: XapiStatementsResource by lazy {
        XapiStatementsResourceRepository(
            local = local.statements,
            remote = remote.statements,
            remoteWriteQueue = remoteWriteQueue,
        )
    }
    override val agents: XapiAgentsResource = local.agents

    override val activities: XapiActivitiesResource = local.activities

    override val state: XapiStateResource
        get() = TODO("Not yet implemented")

    override val activityProfile: XapiActivityProfileResource by lazy {
        XapiActivityProfileResourceRepository(
            local = local.activityProfile,
            remote = remote.activityProfile,
            remoteWriteQueue = remoteWriteQueue,
            json = json,
        )
    }

    override fun close() {
        remote.close()
        local.close()
    }
}