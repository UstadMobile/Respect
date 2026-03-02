package world.respect.datalayer.repository.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.school.opds.OpdsPublicationDataSource
import world.respect.datalayer.school.opds.OpdsPublicationDataSourceLocal
import world.respect.lib.opds.model.OpdsPublication

class OpdsPublicationDataSourceRepository(
    private val local: OpdsPublicationDataSourceLocal,
    private val remote: OpdsPublicationDataSource,
): OpdsPublicationDataSource {

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): Flow<DataLoadState<OpdsPublication>> {
        return local.getByUrlAsFlow(
            url = url,
            params = params,
            referrerUrl = referrerUrl,
            expectedPublicationId = expectedPublicationId,
        ).combineWithRemote(
            remoteFlow = remote.getByUrlAsFlow(
                url = url,
                params = params,
                referrerUrl = referrerUrl,
                expectedPublicationId = expectedPublicationId,
            ).onEach { remoteData ->
                if(remoteData is DataReadyState) {
                    local.updateOpdsPublication(remoteData)
                }
            }
        )
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): DataLoadState<OpdsPublication> {
        val remoteData = remote.getByUrl(url, params, referrerUrl, expectedPublicationId)
        if(remoteData is DataReadyState)
            local.updateOpdsPublication(remoteData)

        return local.getByUrl(url, params, referrerUrl, expectedPublicationId)
    }

}
