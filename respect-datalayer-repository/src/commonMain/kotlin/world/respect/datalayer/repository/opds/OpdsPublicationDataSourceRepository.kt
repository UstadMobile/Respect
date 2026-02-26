package world.respect.datalayer.repository.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
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
        return remote.getByUrlAsFlow(url, params, referrerUrl, expectedPublicationId)
    }
}
