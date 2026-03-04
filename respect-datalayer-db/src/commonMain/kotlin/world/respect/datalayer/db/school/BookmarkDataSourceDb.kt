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

class BookmarkDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
): BookmarkDataSource {

    val personUidNum = uidNumberMapper(authenticatedUser.guid)

    override fun getBookmarkStatus(url: Url): Flow<Boolean> {

        val urlHash: Long = uidNumberMapper(url.toString())

        return schoolDb.getBookmarkDao()
            .getBookmarkStatus(urlHash,personUidNum)
    }


    override suspend fun store(bookmark: Bookmark) {

        val dao = schoolDb.getBookmarkDao()

        val urlHash = uidNumberMapper(bookmark.learningUnitManifestUrl)

        val exists = dao.getBookmarkStatus(urlHash,personUidNum).first()

        if (exists) {
            dao.updateBookmark(
                personUidNum=personUidNum,
                urlHash = urlHash,
                status = StatusEnum.TO_BE_DELETED.flag,
            )
        } else {
            dao.insertBookmark(
                bookmark.toEntities(uidNumberMapper)
            )
        }
    }

    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return schoolDb.getBookmarkDao().getAllBookmarks(personUidNum)
            .map { entities ->
                entities.map { entity ->
                    entity.toModel()
                }
            }
    }

  override suspend fun removeBookmark(url: String) {
      val urlHash = uidNumberMapper(url)
      schoolDb.getBookmarkDao()
          .updateBookmark(
              personUidNum=personUidNum,
              urlHash = urlHash,
              status = StatusEnum.TO_BE_DELETED.flag
          )
  }

}