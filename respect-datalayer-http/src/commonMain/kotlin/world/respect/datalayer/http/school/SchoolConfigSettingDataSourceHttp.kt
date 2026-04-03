package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
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
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.SchoolConfigSettingDataSource
import world.respect.datalayer.school.model.SchoolConfigSetting
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class SchoolConfigSettingDataSourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : SchoolConfigSettingDataSource, SchoolUrlBasedDataSource {

    private suspend fun SchoolConfigSettingDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(SchoolConfigSettingDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
                keys?.let { parameters.appendAll(DataLayerParams.KEYS, it) }
            }
            .build()
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolConfigSetting> {
        return httpClient.getAsDataLoadState<List<SchoolConfigSetting>>(
            SchoolConfigSettingDataSource.GetListParams(
                keys = listOf(guid)
            ).urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.firstOrNotLoaded()
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolConfigSetting>>> {
        return httpClient.getDataLoadResultAsFlow<List<SchoolConfigSetting>>(
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
        params: SchoolConfigSettingDataSource.GetListParams
    ): DataLoadState<List<SchoolConfigSetting>> {
        return httpClient.getAsDataLoadState<List<SchoolConfigSetting>>(
            url = params.urlWithParams(),
            validationHelper = validationHelper,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override suspend fun store(list: List<SchoolConfigSetting>) {
        httpClient.post(
            url = respectEndpointUrl(SchoolConfigSettingDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}
