package world.respect.datalayer.school.xapi

import world.respect.datalayer.DataLoadParams
import world.respect.lib.xapi.model.XapiActor

interface XapiActorDataSource {

    suspend fun getPerson(
        actor: XapiActor,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    )

}