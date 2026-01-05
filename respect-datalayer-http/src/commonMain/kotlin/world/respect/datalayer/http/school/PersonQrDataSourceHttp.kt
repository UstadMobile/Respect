package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
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
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonQrDataSource
import world.respect.datalayer.school.model.PersonBadge
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.shared.params.GetListCommonParams

class PersonQrDataSourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : PersonQrDataSource, SchoolUrlBasedDataSource {

    private suspend fun PersonQrDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(PersonQrDataSource.ENDPOINT_NAME)).apply {
            parameters.appendCommonListParams(common)
        }.build()
    }

    override suspend fun listAll(listParams: PersonQrDataSource.GetListParams): DataLoadState<List<PersonBadge>> {
        return httpClient.getAsDataLoadState(
            url = listParams.urlWithParams(),
            validationHelper = validationHelper,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override fun listAllAsFlow(
        loadParams: DataLoadParams,
        listParams: PersonQrDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonBadge>>> {
        return httpClient.getDataLoadResultAsFlow(
            urlFn = { listParams.urlWithParams() },
            dataLoadParams = loadParams,
            validationHelper = validationHelper,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<PersonBadge>> {
        return httpClient.getDataLoadResultAsFlow<List<PersonBadge>>(
            urlFn = {
                PersonQrDataSource.GetListParams(
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

    override suspend fun deletePersonBadge(uidNum: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun existsByQrCodeUrl(url: String,uidNum: Long): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun store(list: List<PersonBadge>) {
        httpClient.post(
            respectEndpointUrl(PersonQrDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}