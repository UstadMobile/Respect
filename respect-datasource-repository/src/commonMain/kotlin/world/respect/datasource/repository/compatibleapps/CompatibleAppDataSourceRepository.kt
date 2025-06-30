package world.respect.datasource.repository.compatibleapps

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import world.respect.datasource.DataLoadParams
import world.respect.datasource.DataLoadResult
import world.respect.datasource.DataLoadState
import world.respect.datasource.compatibleapps.CompatibleAppsDataSource
import world.respect.datasource.compatibleapps.CompatibleAppsDataSourceLocal
import world.respect.datasource.compatibleapps.model.RespectAppManifest
import world.respect.datasource.repository.ext.combineLocalWithRemote

class CompatibleAppDataSourceRepository(
    private val local: CompatibleAppsDataSourceLocal,
    private val remote: CompatibleAppsDataSource,
) : CompatibleAppsDataSource{

    override fun getApp(
        manifestUrl: String,
        loadParams: DataLoadParams
    ): Flow<DataLoadState<RespectAppManifest>> {
        return emptyFlow()
    }

    override fun getAddableApps(
        loadParams: DataLoadParams
    ): Flow<DataLoadState<List<DataLoadResult<RespectAppManifest>>>> {
        return local.getAddableApps(loadParams).combineLocalWithRemote(
            remoteFlow = remote.getAddableApps(loadParams),
            onRemoteNewer = {
                local.upsertCompatibleApps(it.data ?: emptyList())
            }
        )
    }

    override fun getLaunchpadApps(loadParams: DataLoadParams): Flow<DataLoadState<List<RespectAppManifest>>> {
        return emptyFlow()
    }

    override suspend fun addAppToLaunchpad(manifestUrl: String) {

    }

    override suspend fun removeAppFromLaunchpad(manifestUrl: String) {

    }
}