package world.respect.datalayer.db.bookmarks

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import world.respect.datalayer.bookmarks.BookmarkDataSource
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.bookmarks.entities.BookmarkEntity
import world.respect.lib.opds.model.Bookmark
import world.respect.libxxhash.XXStringHasher

class BookmarkDataSourceDb(

    private val respectDatabase: RespectAppDatabase,
    private val xxStringHasher: XXStringHasher,
): BookmarkDataSource {

    override fun observeBookmarkStatus(url: Url): Flow<Boolean> {
        val urlHash: String = xxStringHasher.hash(url.toString()).toString()
        return respectDatabase.getBookmarkDao()
            .observeBookmarkStatus(urlHash)
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

        val urlHash: Long = xxStringHasher.hash(url.toString())
        val exists = respectDatabase.getBookmarkDao()
            .observeBookmarkStatus(url.toString())
            .first()

        if (exists) {
            respectDatabase.getBookmarkDao().deleteBookmark(url.toString())
        } else {
            respectDatabase.getBookmarkDao().insertBookmark(
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
                    refererUrl = refererUrl?.toString()
                )
            )
        }
    }

    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return respectDatabase.getBookmarkDao().observeAllBookmarks()
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

    override suspend fun removeBookmark(url: String) {
        respectDatabase.getBookmarkDao().deleteBookmark(url)
    }
}