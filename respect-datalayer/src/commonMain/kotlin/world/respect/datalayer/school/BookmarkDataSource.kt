package world.respect.datalayer.school

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.school.model.Bookmark
import kotlin.time.Instant

interface BookmarkDataSource {

    fun getBookmarkStatus(url: Url): Flow<Boolean>

    suspend fun store(bookmark: Bookmark)

    suspend fun removeBookmark(
        uid: String,
        lastModified: Instant
    )

    fun getAllBookmarks(): Flow<List<Bookmark>>
}