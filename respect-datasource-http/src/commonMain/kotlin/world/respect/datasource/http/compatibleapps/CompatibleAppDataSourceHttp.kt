package world.respect.datasource.http.compatibleapps

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import world.respect.datasource.DataLoadMetaInfo
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.DataLoadState
import world.respect.datasource.DataLoadingState
import world.respect.datasource.LoadingStatus
import world.respect.datasource.compatibleapps.CompatibleAppsDataSource
import world.respect.datasource.compatibleapps.model.RespectAppManifest
import world.respect.datasource.ext.getDataLoadResult

class CompatibleAppDataSourceHttp(
    private val httpClient: HttpClient,
    private val defaultCompatibleAppListUrl: String,
): CompatibleAppsDataSource {

    override fun getApp(
        manifestUrl: String,
        loadParams: DataLoadParams,
    ): Flow<DataLoadState<RespectAppManifest>> {
        return flow {
            emit(DataLoadingState())
            emit(httpClient.getDataLoadResult(Url(manifestUrl)))
        }
    }

    override fun getAddableApps(
        loadParams: DataLoadParams
    ): Flow<DataLoadState<List<DataLoadResult<RespectAppManifest>>>> {
        return flow {
            emit(DataLoadingState())
            val respectAppUrls: List<String> = httpClient.get(defaultCompatibleAppListUrl).body()
            val manifests: List<DataLoadResult<RespectAppManifest>> = respectAppUrls.mapNotNull { url ->
                try {
                    httpClient.getDataLoadResult(Url(url))
                }catch(e: Throwable) {
                    //Log
                    println("getAddableApps: error: $e")
                    null
                }
            }
            emit(
                DataLoadResult(
                    data = manifests,
                    metaInfo = DataLoadMetaInfo(
                        status = LoadingStatus.LOADED,
                        lastModified = manifests.maxOfOrNull { it.metaInfo.lastModified } ?: -1L
                    ),
                )
            )
        }
    }

    override fun getLaunchpadApps(loadParams: DataLoadParams): Flow<DataLoadState<List<RespectAppManifest>>> {
        return emptyFlow()
    }

    override suspend fun addAppToLaunchpad(manifestUrl: String) {
        //do nothing - does not run remotely
    }

    override suspend fun removeAppFromLaunchpad(manifestUrl: String) {
        //do nothing - does not run remotely
    }

}