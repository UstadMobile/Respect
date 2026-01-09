package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.util.reflect.typeInfo
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.firstOrNotLoaded
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.ext.useValidationCacheControl
import world.respect.datalayer.http.ext.appendCommonListParams
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.InviteDataSource
import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

class InviteDataSourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
    ) : InviteDataSource, SchoolUrlBasedDataSource {

    private suspend fun InviteDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(InviteDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
            }
            .build()
    }
    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: InviteDataSource.GetListParams
    ): IPagingSourceFactory<Int, Invite> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { params.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<Invite>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                }
            )
        }
    }
    override suspend fun findByGuid(guid: String): DataLoadState<Invite>{
            return httpClient.getAsDataLoadState<List<Invite>>(
                InviteDataSource.GetListParams(
                    GetListCommonParams(guid = guid)
                ).urlWithParams()
            ) {
                useTokenProvider(tokenProvider)
                useValidationCacheControl(validationHelper)
            }.firstOrNotLoaded()
        }

    override suspend fun findByCode(code: String): DataLoadState<Invite> {
        return httpClient.getAsDataLoadState<List<Invite>>(
            InviteDataSource.GetListParams(
                inviteCode = code
            ).urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.firstOrNotLoaded()
    }

    override suspend fun store(list: List<Invite>) {
        httpClient.post(
            url = respectEndpointUrl(InviteDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}
