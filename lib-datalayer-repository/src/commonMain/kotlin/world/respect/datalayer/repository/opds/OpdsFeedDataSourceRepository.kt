package world.respect.datalayer.repository.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.repository.ext.copyToValidateOnRemote
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.combineWithRemote
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.datalayer.school.opds.OpdsFeedDataSourceLocal
import world.respect.lib.opds.model.OpdsFeed

class OpdsFeedDataSourceRepository(
    val local: OpdsFeedDataSourceLocal,
    val remote: OpdsFeedDataSource,
): OpdsFeedDataSource  {

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams
    ): Flow<DataLoadState<OpdsFeed>> {
        return local.getByUrlAsFlow(
            url = url,
            params = params
        ).combineWithRemote(
            remoteFlow = remote.getByUrlAsFlow(
                url = url,
                params = params
            ).onEach { remoteData ->
                if(remoteData is DataReadyState) {
                    local.updateLocal(url, remoteData)
                }
            }
        )
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams
    ): DataLoadState<OpdsFeed> {
        val localData = local.getByUrl(url, params)
        val remoteData = remote.getByUrl(
            url = url,
            params = params.copyToValidateOnRemote(localData.metaInfo)
        )

        return if(remoteData is DataReadyState) {
            local.updateLocal(url, remoteData)
            local.getByUrl(url = url, params = params)
        }else {
            localData
        }
    }
}