package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.DataLayerParams
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.firstOrNotLoaded
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.ext.useValidationCacheControl
import world.respect.datalayer.http.ext.appendCommonListParams
import world.respect.datalayer.http.ext.appendIfNotNull
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.ChangeHistoryDataSource
import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

class ChangeHistoryDataSourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : ChangeHistoryDataSource, SchoolUrlBasedDataSource {

    private suspend fun ChangeHistoryDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(ChangeHistoryDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
                parameters.appendIfNotNull(DataLayerParams.FILTER_BY_TABLE, filterByTable?.value)
                parameters.appendIfNotNull(DataLayerParams.FILTER_BY_WHO_GUID, filterByWhoGuid)
            }
            .build()
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<List<ChangeHistoryEntry>> {
        return httpClient.getAsDataLoadState<List<ChangeHistoryEntry>>(
            ChangeHistoryDataSource.GetListParams(
                common = GetListCommonParams(guid = guid)
            ).urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<ChangeHistoryEntry>> {
        return httpClient.getDataLoadResultAsFlow<List<ChangeHistoryEntry>>(
            urlFn = {
                ChangeHistoryDataSource.GetListParams(
                    common = GetListCommonParams(guid = guid)
                ).urlWithParams()
            },
            dataLoadParams = loadParams,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.map {
            it.firstOrNotLoaded()
        }
    }


    override fun listAsPagingSource(
        dataLoadParams: DataLoadParams,
        getListParams: ChangeHistoryDataSource.GetListParams
    ): IPagingSourceFactory<Int, ChangeHistoryEntry> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { getListParams.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<ChangeHistoryEntry>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                },
                logPrefixExtra = { "ChangeHistoryDataSource params=$getListParams" }
            )
        }
    }

    override suspend fun store(list: List<ChangeHistoryEntry>) {
        httpClient.post(
            respectEndpointUrl(ChangeHistoryDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}
