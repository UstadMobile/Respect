package world.respect.datalayer.school.xapi

import world.respect.lib.xapi.model.XapiActor
import kotlin.time.Instant

interface XapiActorDataSourceLocal : XapiActorDataSource {

    suspend fun updateLocal(
        actors: List<XapiActor>,
        timestamp: Instant,
    )

}