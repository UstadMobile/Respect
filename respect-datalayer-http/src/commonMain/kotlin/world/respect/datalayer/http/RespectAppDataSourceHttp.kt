package world.respect.datalayer.http

import io.ktor.client.HttpClient
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.compatibleapps.CompatibleAppsDataSource
import world.respect.datalayer.http.compatibleapps.CompatibleAppDataSourceHttp
import world.respect.datalayer.http.opds.OpdsDataSourceHttp
import world.respect.datalayer.http.schooldirectory.SchoolDirectoryEntryDataSourceHttp
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.opds.OpdsDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class RespectAppDataSourceHttp(
    private val httpClient: HttpClient,
    private val local : RespectAppDataSourceLocal,
    private val defaultCompatibleAppListUrl: String,
    private val compatibleAppsValidationHelper: BaseDataSourceValidationHelper? = null,
): RespectAppDataSource {

    override val compatibleAppsDataSource: CompatibleAppsDataSource by lazy {
        CompatibleAppDataSourceHttp(
            httpClient = httpClient,
            defaultCompatibleAppListUrl = defaultCompatibleAppListUrl,
            validationValidationHelper = compatibleAppsValidationHelper,
        )
    }

    override val opdsDataSource: OpdsDataSource by lazy {
        OpdsDataSourceHttp(
            httpClient = httpClient
        )
    }

    override val schoolDirectoryDataSource: SchoolDirectoryDataSource
        get() = throw IllegalArgumentException("There is no http data source for directory list")

    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource by lazy {
        SchoolDirectoryEntryDataSourceHttp(
            httpClient = httpClient,
            local = local,
        )
    }
}
