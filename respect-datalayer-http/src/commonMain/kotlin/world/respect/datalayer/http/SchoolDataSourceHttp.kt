package world.respect.datalayer.http

import io.ktor.client.HttpClient
import io.ktor.http.Url
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.http.school.IndicatorDataSourceHttp
import world.respect.datalayer.http.school.ClassDataSourceHttp
import world.respect.datalayer.http.school.EnrollmentDataSourceHttp
import world.respect.datalayer.http.school.PersonDataSourceHttp
import world.respect.datalayer.http.school.ReportDataSourceHttp
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class SchoolDataSourceHttp(
    private val schoolUrl: Url,
    private val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper
) : SchoolDataSource {

    override val personDataSource: PersonDataSource by lazy {
        PersonDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val reportDataSource: ReportDataSource by lazy {
        ReportDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryDataSource = schoolDirectoryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }


    override val indicatorDataSource: IndicatorDataSource by lazy {
        IndicatorDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryDataSource = schoolDirectoryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val classDataSource: ClassDataSource by lazy {
        ClassDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val enrollmentDataSource: EnrollmentDataSource by lazy {
        EnrollmentDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }
}