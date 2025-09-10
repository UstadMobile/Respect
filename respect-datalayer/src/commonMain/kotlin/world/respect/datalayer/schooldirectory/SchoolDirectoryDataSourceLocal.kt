package world.respect.datalayer.schooldirectory

import world.respect.datalayer.DataReadyState
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.RespectSchoolDirectory

interface SchoolDirectoryDataSourceLocal: SchoolDirectoryDataSource {

    suspend fun putSchoolDirectoryEntry(
        school: DataReadyState<SchoolDirectoryEntry>,
        directory: RespectSchoolDirectory?
    )

    suspend fun addServerManagedSchool(
        school: SchoolDirectoryEntry,
        dbUrl: String,
    )

    suspend fun getServerManagedDirectory(): RespectSchoolDirectory?

}