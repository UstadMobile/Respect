package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.ext.useValidationCacheControl
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.datalayer.school.model.PersonPasskey
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class PersonPasskeyDataSourceHttp(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : PersonPasskeyDataSource, SchoolUrlBasedDataSource{

    override suspend fun listAll(): DataLoadState<List<PersonPasskey>> {
        return httpClient.getAsDataLoadState<List<PersonPasskey>>(
            url = respectEndpointUrl(PersonPasskeyDataSource.ENDPOINT_NAME),
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override fun listAllAsFlow(): Flow<DataLoadState<List<PersonPasskey>>> {
        return httpClient.getDataLoadResultAsFlow<List<PersonPasskey>>(
            urlFn = { respectEndpointUrl(PersonPasskeyDataSource.ENDPOINT_NAME) },
            dataLoadParams = DataLoadParams(),
            validationHelper = validationHelper
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override suspend fun store(list: List<PersonPasskey>) {
        TODO("Not yet implemented")
    }
}