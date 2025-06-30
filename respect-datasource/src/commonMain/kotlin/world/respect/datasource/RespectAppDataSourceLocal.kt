package world.respect.datasource

import world.respect.datasource.compatibleapps.CompatibleAppsDataSourceLocal

/**
 *
 */
interface RespectAppDataSourceLocal: RespectAppDataSource {

    override val compatibleAppsDataSource: CompatibleAppsDataSourceLocal

}
