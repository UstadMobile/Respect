package world.respect.datalayer.http.opds

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.school.opds.OpdsPublicationDataSource
import world.respect.lib.opds.model.OpdsPublication

class OpdsPublicationDataSourceHttp(
    private val httpClient: HttpClient,
    private val publicationValidationHelper: BaseDataSourceValidationHelper? = null,
) : OpdsPublicationDataSource {

    override fun getByUrlAsFlow(
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