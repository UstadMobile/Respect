package world.respect.datalayer.repository.school

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class SchoolDirectoryDataSourceRepository(
    private val local: SchoolDirectoryDataSourceLocal,
    private val remote: SchoolDirectoryDataSource,
): SchoolDirectoryDataSource{
    override suspend fun allDirectories(): List<RespectSchoolDirectory> {
        TODO("Not yet implemented")
    }

    override suspend fun allSchoolsInDirectory(): List<SchoolDirectoryEntry> {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun searchSchools(text: String): Flow<DataLoadState<List<SchoolDirectoryEntry>>> {
        val remoteResults = remote.searchSchools(text).onEach { loadState ->
            if (loadState is DataReadyState) {
                loadState.data.forEach { School ->
                    local.upsertSchoolDirectoryEntry(
                        DataReadyState(
                            School,
                            DataLoadMetaInfo(
                                lastModified = Clock.System.now().toEpochMilliseconds()
                            )
                        ),
                        directory = null
                    )
                }
            }
        }

        return local.searchSchools(text).combine(remoteResults) { localState, remoteState ->
            localState.combineWithRemote(remoteState)
        }
    }

    override suspend fun getInviteInfo(inviteCode: String): RespectInviteInfo {
        TODO("Not yet implemented")
    }

    override suspend fun getSchoolDirectoryEntryByUrl(url: Url): DataReadyState<SchoolDirectoryEntry>? {
        TODO("Not yet implemented")
    }


}