package world.respect.datalayer.bookmarks

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.lib.opds.model.Bookmark

interface BookmarkDataSource {

    fun observeBookmarkStatus(url: Url): Flow<Boolean>

    suspend fun store(
        url: Url,
        title: String?,
        subtitle: String?,
        appIcon: String,
        appName: String,
        iconUrl: String?,
        appManifestUrl: Url,
        expectedIdentifier: String?,
        refererUrl: Url?,
    )

    fun getAllBookmarks(): Flow<List<Bookmark>>

    suspend fun removeBookmark(url: Long)
}