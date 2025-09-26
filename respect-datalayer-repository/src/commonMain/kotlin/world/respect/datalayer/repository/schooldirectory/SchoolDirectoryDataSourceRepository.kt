package world.respect.datalayer.repository.schooldirectory

import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal

class SchoolDirectoryDataSourceRepository(
    private val local: SchoolDirectoryDataSourceLocal,
    private val remote: SchoolDirectoryDataSource,
) : SchoolDirectoryDataSource{

    override suspend fun allDirectories(): List<RespectSchoolDirectory> {
        return local.allDirectories()
    }

    override suspend fun getInviteInfo(inviteCode: String): RespectInviteInfo {
        return remote.getInviteInfo(inviteCode)
    }

    override suspend fun deleteDirectory(directory: RespectSchoolDirectory) {
        return local.deleteDirectory(directory)

    }

}