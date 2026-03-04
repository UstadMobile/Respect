package world.respect.datalayer.school

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.school.model.Bookmark

interface BookmarkDataSource {

    fun getBookmarkStatus(url: Url): Flow<Boolean>

    suspend fun store(
       bookmark: Bookmark
    )

    fun getAllBookmarks(): Flow<List<Bookmark>>

    suspend fun removeBookmark(url: String)
}