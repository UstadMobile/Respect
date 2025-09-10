package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import world.respect.datalayer.DataErrorResult
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.libutil.ext.resolve
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SchoolDirectoryDataSourceHttp(
    private val httpClient: HttpClient,
    private val local : RespectAppDataSourceLocal
    ) : SchoolDirectoryDataSource {

    override suspend fun allDirectories(): List<RespectSchoolDirectory> {
        TODO("Not yet implemented")
    }

    override suspend fun allSchoolsInDirectory(): List<SchoolDirectoryEntry> {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun searchSchools(text: String): Flow<DataLoadState<List<SchoolDirectoryEntry>>> {
        return flow {
            emit(DataLoadingState())
            try {
                val directories = local.schoolDirectoryDataSource.allDirectories()
                val respectSchools = mutableListOf<SchoolDirectoryEntry>()

                for (dir in directories) {
                    val url = dir.baseUrl.resolve("api/directory/school?name=$text")

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

    @OptIn(ExperimentalTime::class)
    override suspend fun getSchoolDirectoryEntryByUrl(url: Url): DataReadyState<SchoolDirectoryEntry>? {
        return try {
            val apiUrl = url.resolve("api/directory/school/Url")
            val schoolDirectoryEntry: SchoolDirectoryEntry = httpClient.get(apiUrl) {
                parameter("url", url.toString())
            }.body()

            DataReadyState(
                data = schoolDirectoryEntry,
                metaInfo = DataLoadMetaInfo(
                    lastModified = Clock.System.now().toEpochMilliseconds()
                )
            )
        } catch (e: Throwable) {
            println(e.message.toString())
            null
        }
    }


}
