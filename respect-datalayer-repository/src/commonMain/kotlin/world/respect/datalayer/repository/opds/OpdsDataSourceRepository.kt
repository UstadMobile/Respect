package world.respect.datalayer.repository.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.opds.OpdsDataSource
import world.respect.datalayer.opds.OpdsDataSourceLocal
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsPublication
import world.respect.datalayer.ext.combineWithRemote

class OpdsDataSourceRepository(
    private val local: OpdsDataSourceLocal,
    private val remote: OpdsDataSource,
): OpdsDataSource {

    override fun loadOpdsFeed(
        url: Url,
        params: DataLoadParams
    ): Flow<DataLoadState<OpdsFeed>> {
        val remoteFeed = remote.loadOpdsFeed(url, params).onEach { loadState ->
            if(loadState is DataReadyState) {
                local.updateOpdsFeed(loadState)
            }
        }

        return local.loadOpdsFeed(url, params).combine(remoteFeed) { local, remote ->
            local.combineWithRemote(remote)
        }
    }

    override fun loadOpdsPublication(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): Flow<DataLoadState<OpdsPublication>> {
        return remote.loadOpdsPublication(url, params, referrerUrl, expectedPublicationId)
    }
}
