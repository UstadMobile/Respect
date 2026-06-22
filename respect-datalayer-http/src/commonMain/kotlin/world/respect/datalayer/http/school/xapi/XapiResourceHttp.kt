package world.respect.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.lib.xapi.resources.XapiActivitiesResource
import world.respect.lib.xapi.resources.XapiAgentsResource
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStatementsResource

class XapiResourceHttp(
    private val schoolUrl: Url,
    private val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val json: Json,
): XapiResource {

    override val statements: XapiStatementsResource by lazy {
        XapiStatementsResourceHttp(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider = tokenProvider,
            json = json,
        )
    }

    override val agents: XapiAgentsResource
        get() = TODO("Not yet implemented")
    override val activities: XapiActivitiesResource
        get() = TODO("Not yet implemented")

    override fun close() {
        //Does nothing yet
    }
}