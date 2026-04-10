package world.respect.datalayer.school.xapi

import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.school.xapi.model.XapiActor

interface XapiActorDataSource {

    data class GetListParams(
        val actor: XapiActor? = null,
    )

    suspend fun store(
        list: List<XapiActor>
    )

    suspend fun list(
        getListParams: GetListParams,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    )

}