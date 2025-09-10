package world.respect.datalayer.schooldirectory

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.invite.RespectInviteInfo

/**
 * DataSource to access all known directories
 */
interface SchoolDirectoryDataSource {

    suspend fun allDirectories(): List<RespectSchoolDirectory>

    suspend fun allSchoolsInDirectory(): List<SchoolDirectoryEntry>

    suspend fun searchSchools(text: String): Flow<DataLoadState<List<SchoolDirectoryEntry>>>

    suspend fun getInviteInfo(inviteCode: String): RespectInviteInfo

    /**
     * Get the SchoolDirectoryEntry for a given url
     *
     * @param url The URL as per SchoolDirectoryEntry.self
     */
    suspend fun getSchoolDirectoryEntryByUrl(url: Url): DataLoadState<SchoolDirectoryEntry>

}