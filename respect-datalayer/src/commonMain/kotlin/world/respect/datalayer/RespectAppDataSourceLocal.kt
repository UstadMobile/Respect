package world.respect.datalayer

import world.respect.datalayer.compatibleapps.CompatibleAppsDataSourceLocal
import world.respect.datalayer.opds.OpdsDataSourceLocal
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSourceLocal

/**
 *
 */
interface RespectAppDataSourceLocal: RespectAppDataSource {

    override val compatibleAppsDataSource: CompatibleAppsDataSourceLocal

    override val opdsDataSource: OpdsDataSourceLocal

    override val schoolDirectoryDataSource: SchoolDirectoryDataSourceLocal

    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSourceLocal

}
