package world.respect.datalayer.http

import io.ktor.client.HttpClient
import io.ktor.http.Url
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.http.school.PersonDataSourceHttp
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource

class SchoolDataSourceHttp(
    private val schoolUrl: Url,
    private val schoolDirectoryDataSource: SchoolDirectoryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper
) : SchoolDataSource {

    override val personDataSource: PersonDataSource by lazy {
        PersonDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryDataSource = schoolDirectoryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }
}