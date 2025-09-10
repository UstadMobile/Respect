package world.respect.datalayer.http.schooldirectory

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.respect.model.RESPECT_SCHOOL_JSON_PATH
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.libutil.ext.resolve

class SchoolDirectoryDataSourceHttp(
    private val httpClient: HttpClient,
): SchoolDirectoryDataSource{

    override suspend fun allDirectories(): List<RespectSchoolDirectory> {
        TODO("Not yet implemented")
    }

    override suspend fun allSchoolsInDirectory(): List<SchoolDirectoryEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun searchSchools(text: String): Flow<DataLoadState<List<SchoolDirectoryEntry>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getInviteInfo(inviteCode: String): RespectInviteInfo {
        TODO("Not yet implemented")
    }

    override suspend fun getSchoolDirectoryEntryByUrl(
        url: Url
    ): DataLoadState<SchoolDirectoryEntry> {
        return httpClient.getAsDataLoadState<SchoolDirectoryEntry>(
            url.resolve(RESPECT_SCHOOL_JSON_PATH)
        )
    }
}