package world.respect.datalayer.http.compatibleapps

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import world.respect.datalayer.DataErrorResult
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.compatibleapps.CompatibleAppsDataSource
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.libutil.ext.resolve


class CompatibleAppDataSourceHttp(
    private val httpClient: HttpClient,
    defaultCompatibleAppListUrl: String,
    private val validationValidationHelper: BaseDataSourceValidationHelper? = null,
): CompatibleAppsDataSource {

    private val defaultCompatibleAppListUrlObj = Url(defaultCompatibleAppListUrl)

    override suspend fun getApp(
        manifestUrl: Url,
        loadParams: DataLoadParams,
    ): DataLoadState<RespectAppManifest> {
        return httpClient.getAsDataLoadState(manifestUrl, validationValidationHelper)
    }

    override fun getAppAsFlow(
        manifestUrl: Url,
        loadParams: DataLoadParams,
    ): Flow<DataLoadState<RespectAppManifest>> {
        return httpClient.getDataLoadResultAsFlow(
            manifestUrl, loadParams, validationValidationHelper
        )
    }

    override fun getAddableApps(
        loadParams: DataLoadParams
    ): Flow<DataLoadState<List<DataLoadState<RespectAppManifest>>>> {
        return flow {
            emit(DataLoadingState())
            try {
                val respectAppUrls: List<String> = httpClient.get(defaultCompatibleAppListUrlObj)
                    .body()
                val manifests = respectAppUrls.map { manifestHref ->
                    httpClient.getAsDataLoadState<RespectAppManifest>(
                        defaultCompatibleAppListUrlObj.resolve(manifestHref), validationValidationHelper
                    )
                }

                emit(
                    DataReadyState(
                        data = manifests,
                        metaInfo = DataLoadMetaInfo(
                            lastModified = manifests.maxOfOrNull { it.metaInfo.lastModified } ?: -1L
                        ),
                    )
                )
            }catch(e: Throwable) {
                emit(DataErrorResult(e))
            }
        }
    }

}