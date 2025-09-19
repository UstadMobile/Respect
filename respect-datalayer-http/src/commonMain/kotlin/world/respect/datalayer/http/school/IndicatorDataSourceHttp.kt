package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.firstOrNotLoaded
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.http.ext.appendListParams
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.datalayer.shared.params.GetListCommonParams

class IndicatorDataSourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryDataSource: SchoolDirectoryDataSource,
    private val httpClient: HttpClient,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val tokenProvider: AuthTokenProvider,
) : IndicatorDataSource, SchoolUrlBasedDataSource {

    private suspend fun IndicatorDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(IndicatorDataSource.ENDPOINT_NAME))
            .apply { parameters.appendListParams(common) }
            .build()
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String?
    ): Flow<DataLoadState<List<Indicator>>> {
        return httpClient.getDataLoadResultAsFlow<List<Indicator>>(
            urlFn = {
                IndicatorDataSource.GetListParams(
                    GetListCommonParams(searchQuery = searchQuery)
                ).urlWithParams()
            },
            dataLoadParams = loadParams,
            validationHelper = validationHelper,
        ) {
            headers[HttpHeaders.Authorization] =
                "Bearer ${tokenProvider.provideToken().accessToken}"
        }
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Indicator> {
        return httpClient.getAsDataLoadState<List<Indicator>>(
            IndicatorDataSource.GetListParams(
                GetListCommonParams(guid = guid)
            ).urlWithParams()
        ) {
            headers[HttpHeaders.Authorization] =
                "Bearer ${tokenProvider.provideToken().accessToken}"
        }.firstOrNotLoaded()
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Indicator>> {
        return httpClient.getDataLoadResultAsFlow<List<Indicator>>(
            urlFn = {
                IndicatorDataSource.GetListParams(
                    GetListCommonParams(guid = guid)
                ).urlWithParams()
            },
            dataLoadParams = DataLoadParams()
        ) {
            headers[HttpHeaders.Authorization] =
                "Bearer ${tokenProvider.provideToken().accessToken}"
        }.map {
            it.firstOrNotLoaded()
        }
    }

    override suspend fun store(indicator: Indicator) {
        throw IllegalStateException("Indicator-store-http: Not yet supported")
    }

    override suspend fun initializeDefaultIndicators(idGenerator: () -> String) {
        throw IllegalStateException("initializeDefaultIndicators-http: Not yet supported")
    }
}