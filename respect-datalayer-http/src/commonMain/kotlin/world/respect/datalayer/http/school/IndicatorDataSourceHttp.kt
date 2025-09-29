package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.firstOrNotLoaded
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.ext.useValidationCacheControl
import world.respect.datalayer.http.ext.appendCommonListParams
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

class IndicatorDataSourceHttp(
    override val schoolUrl: Url,
    private val httpClient: HttpClient,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val tokenProvider: AuthTokenProvider,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
) : IndicatorDataSource, SchoolUrlBasedDataSource {

    private suspend fun IndicatorDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(IndicatorDataSource.ENDPOINT_NAME))
            .apply { parameters.appendCommonListParams(common) }
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
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.firstOrNotLoaded()
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: IndicatorDataSource.GetListParams
    ): IPagingSourceFactory<Int, Indicator> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { params.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<Indicator>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                },
                tag = "Indicator-HTTP",
            )
        }
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
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.map {
            it.firstOrNotLoaded()
        }
    }

    override suspend fun store(list: List<Indicator>) {
        httpClient.post(
            url = respectEndpointUrl(IndicatorDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }

    override suspend fun initializeDefaultIndicators(idGenerator: () -> String) {
        throw IllegalStateException("initializeDefaultIndicators-http: Not yet supported")
    }
}