package world.respect.datalayer.http.opds

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.map
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.datalayer.school.opds.ext.withAbsoluteSelfUrl
import world.respect.lib.opds.model.OpdsFeed

class OpdsFeedDataSourceHttp(
    private val httpClient: HttpClient,
    private val opdsFeedValidationHelper: BaseDataSourceValidationHelper? = null,
): OpdsFeedDataSource {

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams
    ): Flow<DataLoadState<OpdsFeed>> {
        return httpClient.getDataLoadResultAsFlow<OpdsFeed>(
            url = url,
            dataLoadParams = params,
            validationHelper = opdsFeedValidationHelper,
        ).map { loadResult ->
            /*
             * When the OpdsFeed is persisted to the database, the self url is used to determine the
             * url. The Url is hashed to make the primary key.
             */
            loadResult.map { it.withAbsoluteSelfUrl(url) }
        }
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams
    ): DataLoadState<OpdsFeed> {
        return httpClient.getAsDataLoadState<OpdsFeed>(
            url = url,
            validationHelper = opdsFeedValidationHelper,
        ).map { it.withAbsoluteSelfUrl(url) }
    }

    override suspend fun store(list: List<OpdsFeed>) {
        TODO("OpdsFeedDataSourceHttp.store using HTTP post")
    }

}