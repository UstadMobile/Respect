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
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.model.Report
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

class ReportDataSourceHttp(
    override val schoolUrl: Url,
    private val httpClient: HttpClient,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val tokenProvider: AuthTokenProvider,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
) : ReportDataSource, SchoolUrlBasedDataSource {

    private suspend fun ReportDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(ReportDataSource.ENDPOINT_NAME))
            .apply { parameters.appendCommonListParams(common) }
            .build()
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: ReportDataSource.GetListParams,
        template: Boolean
    ): Flow<DataLoadState<List<Report>>> {
        return httpClient.getDataLoadResultAsFlow<List<Report>>(
            urlFn = {
                ReportDataSource.GetListParams(
                    GetListCommonParams()
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
    ): DataLoadState<Report> {
        return httpClient.getAsDataLoadState<List<Report>>(
            ReportDataSource.GetListParams(
                GetListCommonParams(guid = guid)
            ).urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.firstOrNotLoaded()
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: ReportDataSource.GetListParams,
        template: Boolean
    ): IPagingSourceFactory<Int, Report> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { params.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<Report>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                },
                tag = "Report-HTTP",
            )
        }
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Report>> {
        return httpClient.getDataLoadResultAsFlow<List<Report>>(
            urlFn = {
                ReportDataSource.GetListParams(
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

    override suspend fun delete(guid: String) {
        throw IllegalStateException("Report-delete-http: Not yet supported")
    }

    override suspend fun store(list: List<Report>) {
        httpClient.post(
            respectEndpointUrl(ReportDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }

    override suspend fun initializeTemplates(idGenerator: () -> String) {
        throw IllegalStateException("initializeTemplates-http: Not yet supported")
    }
}