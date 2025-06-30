package world.respect.datasource.compatibleapps

import world.respect.datasource.DataLoadResult
import world.respect.datasource.compatibleapps.model.RespectAppManifest

interface CompatibleAppsDataSourceLocal: CompatibleAppsDataSource {

    suspend fun upsertCompatibleApps(apps: List<DataLoadResult<RespectAppManifest>>)

}