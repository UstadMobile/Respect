package world.respect.datalayer.repository

import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.compatibleapps.CompatibleAppsDataSource
import world.respect.datalayer.opds.OpdsDataSource
import world.respect.datalayer.repository.compatibleapps.CompatibleAppDataSourceRepository
import world.respect.datalayer.repository.opds.OpdsDataSourceRepository

class RespectAppDataSourceRepository(
    private val local: RespectAppDataSourceLocal,
    private val remote: RespectAppDataSource,
): RespectAppDataSource {

    override val compatibleAppsDataSource: CompatibleAppsDataSource by lazy {
        CompatibleAppDataSourceRepository(
            local.compatibleAppsDataSource, remote.compatibleAppsDataSource
        )
    }
    override val opdsDataSource: OpdsDataSource by lazy {
        OpdsDataSourceRepository(local.opdsDataSource, remote.opdsDataSource)
    }
}