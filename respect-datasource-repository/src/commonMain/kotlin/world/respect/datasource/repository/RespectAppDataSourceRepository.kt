package world.respect.datasource.repository

import world.respect.datasource.RespectAppDataSource
import world.respect.datasource.RespectAppDataSourceLocal
import world.respect.datasource.compatibleapps.CompatibleAppsDataSource
import world.respect.datasource.opds.OpdsDataSource
import world.respect.datasource.repository.compatibleapps.CompatibleAppDataSourceRepository

class RespectAppDataSourceRepository(
    private val local: RespectAppDataSourceLocal,
    private val remote: RespectAppDataSource,
): RespectAppDataSource {

    override val compatibleAppsDataSource: CompatibleAppsDataSource by lazy {
        CompatibleAppDataSourceRepository(
            local.compatibleAppsDataSource, remote.compatibleAppsDataSource
        )
    }
    override val opdsDataSource: OpdsDataSource
        get() = TODO("Not yet implemented")
}