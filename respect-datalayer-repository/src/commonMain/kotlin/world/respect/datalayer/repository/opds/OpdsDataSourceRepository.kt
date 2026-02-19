package world.respect.datalayer.repository.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.opds.OpdsDataSource
import world.respect.datalayer.school.opds.OpdsDataSourceLocal
import world.respect.lib.opds.model.OpdsPublication

class OpdsDataSourceRepository(
    private val local: OpdsDataSourceLocal,
    private val remote: OpdsDataSource,
): OpdsDataSource {

    override fun loadOpdsPublication(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): Flow<DataLoadState<OpdsPublication>> {
        return remote.loadOpdsPublication(url, params, referrerUrl, expectedPublicationId)
    }
}
