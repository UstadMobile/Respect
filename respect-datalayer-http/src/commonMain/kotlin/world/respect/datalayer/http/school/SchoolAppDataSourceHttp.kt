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
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.ext.useValidationCacheControl
import world.respect.datalayer.http.ext.appendCommonListParams
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.SchoolAppDataSource
import world.respect.datalayer.school.model.SchoolApp
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory

class SchoolAppDataSourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : SchoolAppDataSource, SchoolUrlBasedDataSource {

    private suspend fun SchoolAppDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(SchoolAppDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
            }
            .build()
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: SchoolAppDataSource.GetListParams,
    ): IPagingSourceFactory<Int, SchoolApp> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { params.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<SchoolApp>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                }
            )
        }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: SchoolAppDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolApp>>> {
        return httpClient.getDataLoadResultAsFlow(
            urlFn = { params.urlWithParams() },
            dataLoadParams = loadParams,
            validationHelper = validationHelper,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolAppDataSource.GetListParams
    ): DataLoadState<List<SchoolApp>> {
        return httpClient.getAsDataLoadState(
            url = params.urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override suspend fun store(list: List<SchoolApp>) {
        httpClient.post(
            respectEndpointUrl(SchoolAppDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}
