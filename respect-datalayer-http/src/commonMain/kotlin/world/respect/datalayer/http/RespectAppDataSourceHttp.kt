package world.respect.datalayer.http

import io.ktor.client.HttpClient
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.compatibleapps.CompatibleAppsDataSource
import world.respect.datalayer.http.compatibleapps.CompatibleAppDataSourceHttp
import world.respect.datalayer.http.opds.OpdsDataSourceHttp
import world.respect.datalayer.networkvalidation.NetworkDataSourceValidationHelper
import world.respect.datalayer.opds.OpdsDataSource

class RespectAppDataSourceHttp(
    private val httpClient: HttpClient,
    private val defaultCompatibleAppListUrl: String,
    private val compatibleAppsValidationHelper: NetworkDataSourceValidationHelper? = null,
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

}
