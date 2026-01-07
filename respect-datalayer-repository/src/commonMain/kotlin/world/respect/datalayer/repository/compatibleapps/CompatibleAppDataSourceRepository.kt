package world.respect.datalayer.repository.compatibleapps

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.compatibleapps.CompatibleAppsDataSource
import world.respect.datalayer.compatibleapps.CompatibleAppsDataSourceLocal
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.datalayer.ext.combineWithRemote

class CompatibleAppDataSourceRepository(
    private val local: CompatibleAppsDataSourceLocal,
    private val remote: CompatibleAppsDataSource,
) : CompatibleAppsDataSource{

    override suspend fun getApp(
        manifestUrl: Url,
        loadParams: DataLoadParams
    ): DataLoadState<RespectAppManifest> {
        val localResult = local.getApp(manifestUrl, loadParams)
        val remoteResult = remote.getApp(manifestUrl, loadParams)
        return if(remoteResult is DataReadyState) {
            local.upsertCompatibleApps(listOf(remoteResult))
            remoteResult
        }else {
            localResult.combineWithRemote(remoteResult)
        }
    }

    override fun getAppAsFlow(
        manifestUrl: Url,
        loadParams: DataLoadParams
    ): Flow<DataLoadState<RespectAppManifest>> {
        val remoteFlow = remote.getAppAsFlow(manifestUrl, loadParams).onEach {
            (it as? DataReadyState)?.also { appRemote ->
                local.upsertCompatibleApps(listOf(appRemote))
            }
        }

        return local.getAppAsFlow(manifestUrl, loadParams).combine(remoteFlow) { local, remote ->
            local.combineWithRemote(remote)
        }
    }

    override fun getAddableApps(
        loadParams: DataLoadParams
    ): Flow<DataLoadState<List<DataLoadState<RespectAppManifest>>>> {
        val remote = remote.getAddableApps(loadParams).onEach {
            (it as? DataReadyState)?.also { apps ->
                local.upsertCompatibleApps(apps.data)
            }
        }

        return local.getAddableApps(loadParams).combine(remote) { local, remote ->
            local.combineWithRemote(remote)
        }
    }

}