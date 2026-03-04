package world.respect.datalayer.db.school

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.school.BookmarkDataSource
import world.respect.datalayer.school.model.StatusEnum
import world.respect.datalayer.school.model.Bookmark
import kotlin.time.Instant
class BookmarkDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : BookmarkDataSource {

    private val personUid: String = authenticatedUser.guid
    private val personUidNum: Long = uidNumberMapper(personUid)

    override fun getBookmarkStatus(url: Url): Flow<Boolean> {
        val manifestUrl = url.toString()
        val uid = "$personUid|$manifestUrl"
        val uidNum = uidNumberMapper(uid)

        return schoolDb.getBookmarkDao()
            .observeBookmarkStatusByUid(uidNum)
    }

    override suspend fun store(bookmark: Bookmark) {
        val entity = bookmark.toEntities(uidNumberMapper)
        schoolDb.getBookmarkDao().upsert(entity)
    }

    override suspend fun removeBookmark(
        uid: String,
        lastModified: Instant
    ) {
        val uidNum = uidNumberMapper(uid)

        schoolDb.getBookmarkDao().updateStatus(
            uidNum = uidNum,
            status = StatusEnum.TO_BE_DELETED,
            lastModified = lastModified
        )
    }

    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return schoolDb.getBookmarkDao()
            .observeBookmarks(personUidNum)
            .map { list -> list.map { it.toModel() } }
    }
}