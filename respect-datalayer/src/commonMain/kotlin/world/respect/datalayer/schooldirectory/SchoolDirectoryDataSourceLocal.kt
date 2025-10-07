package world.respect.datalayer.schooldirectory

import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.RespectSchoolDirectory

interface SchoolDirectoryDataSourceLocal: SchoolDirectoryDataSource {

    suspend fun setServerManagedSchoolConfig(
        school: SchoolDirectoryEntry,
        dbUrl: String,
    )

    suspend fun getServerManagedDirectory(): RespectSchoolDirectory?

}