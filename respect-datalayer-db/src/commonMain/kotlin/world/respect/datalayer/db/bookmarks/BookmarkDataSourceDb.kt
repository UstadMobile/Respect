package world.respect.datalayer.db.bookmarks

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.bookmarks.BookmarkDataSource
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.bookmarks.adapters.toBookmark
import world.respect.datalayer.db.bookmarks.adapters.toBookmarkEntity
import world.respect.datalayer.school.model.StatusEnum
import world.respect.lib.opds.model.Bookmark

class BookmarkDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
): BookmarkDataSource {

    override fun getBookmarkStatus(url: Url): Flow<Boolean> {
        val urlHash: Long = uidNumberMapper(url.toString())
        return schoolDb.getBookmarkDao()
            .getBookmarkStatus(urlHash)
    }

  

    override suspend fun store(bookmark: Bookmark) {

        val dao = schoolDb.getBookmarkDao()

        val urlHash = uidNumberMapper(bookmark.learningUnitUrl)

        val exists = dao.getBookmarkStatus(urlHash).first()

        if (exists) {
            dao.updateBookmark(
                urlHash = urlHash,
                status = StatusEnum.TO_BE_DELETED.flag
            )
        } else {
            dao.insertBookmark(
                bookmark.toBookmarkEntity(urlHash)
            )
        }
    }

    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return schoolDb.getBookmarkDao().getAllBookmarks()
            .map { entities ->
                entities.map { entity ->
                    entity.toBookmark()
                }
            }
    }

  override suspend fun removeBookmark(url: String) {
      val urlHash = uidNumberMapper(url)
      schoolDb.getBookmarkDao()
          .updateBookmark(
              urlHash = urlHash,
              status = StatusEnum.TO_BE_DELETED.flag
          )
  }

}