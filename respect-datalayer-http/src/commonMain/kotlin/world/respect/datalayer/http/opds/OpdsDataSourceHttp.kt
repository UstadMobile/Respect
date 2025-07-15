package world.respect.datalayer.http.opds

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.networkvalidation.NetworkDataSourceValidationHelper
import world.respect.datalayer.opds.OpdsDataSource
import world.respect.datalayer.opds.model.OpdsFeed
import world.respect.datalayer.opds.model.OpdsPublication

class OpdsDataSourceHttp(
    private val httpClient: HttpClient,
    private val feedValidationHelper: NetworkDataSourceValidationHelper? = null,
    private val publicationValidationHelper: NetworkDataSourceValidationHelper? = null,
) : OpdsDataSource {

    override fun loadOpdsFeed(
        url: Url,
        params: DataLoadParams
    ): Flow<DataLoadState<OpdsFeed>> {
        return httpClient.getDataLoadResultAsFlow(
            url = url,
            dataLoadParams = params,
            validationHelper = feedValidationHelper,
        )
    }

    override fun loadOpdsPublication(
        url: Url,
        params: DataLoadParams,
        referrerUrl: Url?,
        expectedPublicationId: String?
    ): Flow<DataLoadState<OpdsPublication>> {
        return httpClient.getDataLoadResultAsFlow(
            url = url,
            dataLoadParams = params,
            validationHelper = publicationValidationHelper,
        )
    }
}