package world.respect.datalayer.repository.schooldirectory

import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal

class SchoolDirectoryDataSourceRepository(
    private val local: SchoolDirectoryDataSourceLocal,
    private val remote: SchoolDirectoryDataSource,
) : SchoolDirectoryDataSource{

    override suspend fun allDirectories(): List<RespectSchoolDirectory> {
        return local.allDirectories()
    }

}