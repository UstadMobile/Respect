package world.respect.datalayer.http.schooldirectory

import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import world.respect.datalayer.DataErrorResult
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataLoadingState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.respect.model.RESPECT_SCHOOL_JSON_PATH
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.libutil.ext.resolve
import kotlin.collections.map

class SchoolDirectoryEntryDataSourceHttp(
    private val httpClient: HttpClient,
    private val local : RespectAppDataSourceLocal,
): SchoolDirectoryEntryDataSource {

    /**
     * List all available SchoolDirectoryEntry(s). This will (concurrently) send requests to all
     * known school directories (avoids delay fetching from working servers if one or more other
     * servers are unreachable).
     */
    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: SchoolDirectoryEntryDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolDirectoryEntry>>> {
        return flow {
            val directories = local.schoolDirectoryDataSource.allDirectories()
            val flows = directories.map { dir ->
                httpClient.getDataLoadResultAsFlow<List<SchoolDirectoryEntry>>(
                    url = dir.baseUrl.appendEndpointSegments("api/directory/school"),
                    dataLoadParams = loadParams,
                ) {
                    headers[HttpHeaders.CacheControl] = "no-store"
                }
            }

            emitAll(
                combine(flows = flows) { dataLoadStates ->
                    val data = buildList {
                        dataLoadStates.forEach {
                            it.dataOrNull()?.also(::addAll)
                        }
                    }

                    when {
                        dataLoadStates.all { it is DataReadyState } -> {
                            DataReadyState(data = data)
                        }

                        dataLoadStates.any { it is DataLoadingState } -> {
                            DataLoadingState(partialData = data)
                        }

                        data.isEmpty() && dataLoadStates.any { it is DataErrorResult } -> {
                            DataErrorResult(
                                error = dataLoadStates.firstNotNullOfOrNull {
                                    (it as? DataErrorResult)?.error
                                } ?: IllegalStateException()
                            )
                        }

                        else -> {
                            DataReadyState(data = data)
                        }
                    }
                }
            )
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

    override suspend fun getSchoolDirectoryEntryByUrl(
        url: Url
    ): DataLoadState<SchoolDirectoryEntry> {
        return httpClient.getAsDataLoadState<SchoolDirectoryEntry>(
            url.resolve(RESPECT_SCHOOL_JSON_PATH)
        )
    }
}