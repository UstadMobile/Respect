package world.respect.datasource.http

import io.ktor.client.HttpClient
import world.respect.datasource.RespectAppDataSource
import world.respect.datasource.compatibleapps.CompatibleAppsDataSource
import world.respect.datasource.http.compatibleapps.CompatibleAppDataSourceHttp
import world.respect.datasource.opds.OpdsDataSource

class RespectAppDataSourceHttp(
    private val httpClient: HttpClient,
    private val defaultCompatibleAppListUrl: String,
): RespectAppDataSource {

    override val compatibleAppsDataSource: CompatibleAppsDataSource by lazy {
        CompatibleAppDataSourceHttp(
            httpClient = httpClient,
            defaultCompatibleAppListUrl = defaultCompatibleAppListUrl,
        )
    }

    override val opdsDataSource: OpdsDataSource
        get() = TODO("Not yet implemented")
}
