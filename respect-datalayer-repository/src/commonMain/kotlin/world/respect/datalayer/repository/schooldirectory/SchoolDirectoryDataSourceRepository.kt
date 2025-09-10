package world.respect.datalayer.repository.schooldirectory

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
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

    override suspend fun allSchoolsInDirectory(): List<SchoolDirectoryEntry> {
        return local.allSchoolsInDirectory()
    }

    override suspend fun searchSchools(text: String): Flow<DataLoadState<List<SchoolDirectoryEntry>>> {
        return local.searchSchools(text)
    }

    override suspend fun getInviteInfo(inviteCode: String): RespectInviteInfo {
        return local.getInviteInfo(inviteCode)
    }

    override suspend fun getSchoolDirectoryEntryByUrl(
        url: Url
    ): DataLoadState<SchoolDirectoryEntry> {
        return local.getSchoolDirectoryEntryByUrl(url).takeIf { it is DataReadyState }
            ?: remote.getSchoolDirectoryEntryByUrl(url).also {
                if(it is DataReadyState) {
                    local.putSchoolDirectoryEntry(it, null)
                }
            }
    }

}