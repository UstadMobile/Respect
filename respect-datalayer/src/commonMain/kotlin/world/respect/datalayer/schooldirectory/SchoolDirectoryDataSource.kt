package world.respect.datalayer.schooldirectory

import world.respect.datalayer.respect.model.RespectSchoolDirectory

/**
 * DataSource to access all known directories
 */
interface SchoolDirectoryDataSource {

    suspend fun insertOrIgnore(
        schoolDirectory: RespectSchoolDirectory,
        clearOthers: Boolean = false,
    )

    suspend fun allDirectories(): List<RespectSchoolDirectory>


}