package world.respect.datalayer.school

import io.ktor.http.Url
import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Bookmark
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

interface BookmarkDataSource : WritableDataSource<Bookmark> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val personUid: String? = null,
        val includeDeleted: Boolean = false,
    ) {
        companion object {
            fun fromParams(params: StringValues): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params),
                    personUid = params[PERSON_UID],
                    includeDeleted = params[INCLUDE_DELETED]?.toBoolean() ?: false,
                )
            }
        }
    }

    fun getBookmarkStatus(
        personUid: String,
        url: Url
    ): Flow<Boolean>

    override suspend fun store(
        list: List<Bookmark>
    )

    suspend fun list(
        loadParams: DataLoadParams,
        listParams: GetListParams,
    ): DataLoadState<List<Bookmark>>

    suspend fun findBookmarksWithMissingPublication(
        personUid: String
    ): List<Bookmark>

    fun listAsFlow(
        loadParams: DataLoadParams = DataLoadParams(),
        listParams: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<Bookmark>>>

    companion object {

        const val ENDPOINT_NAME = "bookmark"
        const val PERSON_UID = "personUid"
        const val INCLUDE_DELETED = "includeDeleted"

    }
}
