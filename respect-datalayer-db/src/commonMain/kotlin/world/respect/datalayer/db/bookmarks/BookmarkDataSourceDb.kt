package world.respect.datalayer.db.bookmarks

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.bookmarks.BookmarkDataSource
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.bookmarks.entities.BookmarkEntity
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

    override suspend fun store(
        url: Url,
        title: String?,
        subtitle: String?,
        appIcon: String,
        appName: String,
        iconUrl: String?,
        appManifestUrl: Url,
        expectedIdentifier: String?,
        refererUrl: Url?
    ) {

        val urlHash: Long = uidNumberMapper(url.toString())

        val exists = schoolDb.getBookmarkDao()
            .getBookmarkStatus(urlHash)
            .first()

        if (exists) {
            schoolDb.getBookmarkDao().updateBookmark(
                urlHash = urlHash,
                status = StatusEnum.TO_BE_DELETED.flag
            )
        } else {
            schoolDb.getBookmarkDao().insertBookmark(
                BookmarkEntity(
                    urlHash = urlHash,
                    learningUnitUrl = url.toString(),
                    title = title,
                    subtitle = subtitle,
                    appIcon = appIcon,
                    appName = appName,
                    iconUrl = iconUrl,
                    appManifestUrl = appManifestUrl.toString(),
                    expectedIdentifier = expectedIdentifier,
                    refererUrl = refererUrl?.toString(),
                    status = StatusEnum.ACTIVE.flag,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return schoolDb.getBookmarkDao().getAllBookmarks()
            .map { entities ->
                entities.map { entity ->
                    Bookmark(
                        url = entity.urlHash,
                        learningUnitUrl = entity.learningUnitUrl,
                        title = entity.title,
                        subtitle = entity.subtitle,
                        appIcon = entity.appIcon,
                        appName = entity.appName,
                        iconUrl = entity.iconUrl,
                        appManifestUrl = entity.appManifestUrl,
                        expectedIdentifier = entity.expectedIdentifier,
                        refererUrl = entity.refererUrl )
                }
            }
    }

  override suspend fun removeBookmark(url: Long) {
      schoolDb.getBookmarkDao()
          .updateBookmark(
              urlHash = url,
              status = StatusEnum.TO_BE_DELETED.flag
          )
  }

}