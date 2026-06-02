package world.respect.datalayer.school.xapi

import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.resources.XapiAgentsResource
import kotlin.time.Instant

interface XapiActorDataSourceLocal : XapiAgentsResource {

    suspend fun updateLocal(
        actors: List<XapiActor>,
        timestamp: Instant,
    )

}