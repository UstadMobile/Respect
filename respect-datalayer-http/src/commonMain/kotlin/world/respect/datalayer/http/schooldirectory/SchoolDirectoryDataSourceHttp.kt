package world.respect.datalayer.http.schooldirectory

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import world.respect.datalayer.DataErrorResult
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.respect.model.RESPECT_SCHOOL_JSON_PATH
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.libutil.ext.resolve
import kotlin.time.Clock

class SchoolDirectoryDataSourceHttp(
    private val httpClient: HttpClient,
    private val local : RespectAppDataSourceLocal
): SchoolDirectoryDataSource{
    override suspend fun allDirectories(): List<RespectSchoolDirectory> {
        TODO("Not yet implemented")
    }

    override suspend fun allSchoolsInDirectory(): List<SchoolDirectoryEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun searchSchools(text: String): Flow<DataLoadState<List<SchoolDirectoryEntry>>> {
        return flow {
            emit(DataLoadingState())
            try {
                val directories = local.schoolDirectoryDataSource.allDirectories()
                val respectSchools = mutableListOf<SchoolDirectoryEntry>()

                for (dir in directories) {
                    //baseUrl https://directory.example.org/path/ (WORKS)
                    //baseUrl https://directory.example.org/path (WORKS)

                    val url = URLBuilder(dir.baseUrl.appendEndpointSegments("api/directory/school"))
                        .apply {
                            parameters["name"] = text
                        }
                        .build()

                    val schools: List<SchoolDirectoryEntry> = httpClient.get(url).body()
                    respectSchools += schools
                }
                emit(
                    DataReadyState(
                        data = respectSchools,
                        metaInfo = DataLoadMetaInfo(
                            lastModified = Clock.System.now().toEpochMilliseconds()
                        )
                    )
                )
            } catch (e: Throwable) {
                emit(DataErrorResult(e))
            }
        }
    }

    override suspend fun getInviteInfo(inviteCode: String): RespectInviteInfo {
        val directories = local.schoolDirectoryDataSource.allDirectories()

        for (dir in directories) {
            val url = dir.baseUrl.resolve("api/directory/invite?code=$inviteCode")
            try {
                return httpClient.get(url).body()
            } catch (e: Throwable) {
                println("${e.message}")
            }
        }
        throw IllegalStateException("Invite not found for code=$inviteCode")
    }

    override suspend fun getSchoolDirectoryEntryByUrl(
        url: Url
    ): DataLoadState<SchoolDirectoryEntry> {
        return httpClient.getAsDataLoadState<SchoolDirectoryEntry>(
            url.resolve(RESPECT_SCHOOL_JSON_PATH)
        )
    }
}