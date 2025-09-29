package world.respect.datalayer.http.schooldirectory

import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.respect.model.RESPECT_SCHOOL_JSON_PATH
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.libutil.ext.resolve

class SchoolDirectoryEntryDataSourceHttp(
    private val httpClient: HttpClient,
    private val local : RespectAppDataSourceLocal,
): SchoolDirectoryEntryDataSource {

    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: SchoolDirectoryEntryDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolDirectoryEntry>>> {
        return flow {
            val directories = local.schoolDirectoryDataSource.allDirectories()
            val listEntries = directories.map { dir ->
                httpClient.getAsDataLoadState<List<SchoolDirectoryEntry>>(
                    dir.baseUrl.appendEndpointSegments("api/directory/school")
                ) {
                    headers[HttpHeaders.CacheControl] = "no-store"
                }
            }

            emit(DataReadyState(
                data = listEntries.flatMap {
                    it.dataOrNull() ?: emptyList()
                }
            ))
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: SchoolDirectoryEntryDataSource.GetListParams
    ): DataLoadState<List<SchoolDirectoryEntry>> {
        val directories = local.schoolDirectoryDataSource.allDirectories()
        val listEntries = directories.map { dir ->
            httpClient.getAsDataLoadState<List<SchoolDirectoryEntry>>(
                dir.baseUrl.appendEndpointSegments("api/directory/school")
            ) {
                headers[HttpHeaders.CacheControl] = "no-store"
            }
        }

        return DataReadyState(
            data = listEntries.flatMap {
                it.dataOrNull() ?: emptyList()
            }
        )
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: SchoolDirectoryEntryDataSource.GetListParams
    ): IPagingSourceFactory<Int, SchoolDirectoryEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDirectory(directory: SchoolDirectoryEntry) {
        TODO("Not yet implemented")
    }

    override suspend fun insertDirectoryEntry(directory: SchoolDirectoryEntry) {
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