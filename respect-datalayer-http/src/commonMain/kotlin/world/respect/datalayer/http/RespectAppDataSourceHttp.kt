package world.respect.datalayer.http

import io.ktor.client.HttpClient
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.compatibleapps.CompatibleAppsDataSource
import world.respect.datalayer.http.compatibleapps.CompatibleAppDataSourceHttp
import world.respect.datalayer.http.opds.OpdsDataSourceHttp
import world.respect.datalayer.http.schooldirectory.SchoolDirectoryDataSourceHttp
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.datalayer.opds.OpdsDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource

class RespectAppDataSourceHttp(
    private val httpClient: HttpClient,
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

    override val schoolDirectoryDataSource: SchoolDirectoryDataSource by lazy {
        SchoolDirectoryDataSourceHttp(
            httpClient = httpClient
        )
    }
}
