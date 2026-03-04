package world.respect.datalayer.db.school

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.school.BookmarkDataSource
import world.respect.datalayer.school.model.StatusEnum
import world.respect.datalayer.school.model.Bookmark
import kotlin.time.Instant

class BookmarkDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : BookmarkDataSource {

    private val personUid: String = authenticatedUser.guid

    override fun getBookmarkStatus(url: Url): Flow<Boolean> {
        val manifestUrl = url.toString()

        return schoolDb.getBookmarkDao()
            .observeBookmarkStatus(personUid, manifestUrl)
    }

    override suspend fun store(bookmark: Bookmark) {
        schoolDb.getBookmarkDao()
            .upsert(bookmark.toEntities())
    }

    override suspend fun removeBookmark(
        manifestUrl: String,
        lastModified: Instant
    ) {
        schoolDb.getBookmarkDao().updateStatus(
            personUid = personUid,
            manifestUrl = manifestUrl,
            status = StatusEnum.TO_BE_DELETED,
            lastModified = lastModified
        )
    }

    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return schoolDb.getBookmarkDao()
            .observeBookmarks(personUid)
            .map { list -> list.map { it.toModel() } }
    }
}