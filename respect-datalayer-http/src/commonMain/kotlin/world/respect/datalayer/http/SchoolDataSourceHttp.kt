package world.respect.datalayer.http

import io.ktor.client.HttpClient
import io.ktor.http.Url
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.http.school.AssignmentDataSourceHttp
import world.respect.datalayer.http.school.ClassDataSourceHttp
import world.respect.datalayer.http.school.EnrollmentDataSourceHttp
import world.respect.datalayer.http.school.InviteDataSourceHttp
import world.respect.datalayer.http.school.PersonDataSourceHttp
import world.respect.datalayer.http.school.PersonPasskeyDataSourceHttp
import world.respect.datalayer.http.school.PersonPasswordDataSourceHttp
import world.respect.datalayer.http.school.SchoolAppDataSourceHttp
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.AssignmentDataSource
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.InviteDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.datalayer.school.PersonPasswordDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.SchoolAppDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class SchoolDataSourceHttp(
    private val schoolUrl: Url,
    private val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper
) : SchoolDataSource {

    override val schoolAppDataSource: SchoolAppDataSource by lazy {
        SchoolAppDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val personDataSource: PersonDataSource by lazy {
        PersonDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val personPasskeyDataSource: PersonPasskeyDataSource by lazy {
        PersonPasskeyDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val personPasswordDataSource: PersonPasswordDataSource by lazy {
        PersonPasswordDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val reportDataSource: ReportDataSource
        get() = TODO("Not yet implemented")

    override val indicatorDataSource: IndicatorDataSource
        get() = TODO("Not yet implemented")

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

    override val assignmentDataSource: AssignmentDataSource by lazy {
        AssignmentDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            validationHelper = validationHelper,
        )
    }

    override val inviteDataSource: InviteDataSource by lazy {
        InviteDataSourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
        )
    }
}
