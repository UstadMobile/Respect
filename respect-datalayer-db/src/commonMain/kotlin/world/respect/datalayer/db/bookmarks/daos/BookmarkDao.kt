package world.respect.datalayer.db.bookmarks.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.bookmarks.entities.BookmarkEntity

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM BookmarkEntity WHERE urlHash = :urlHash")
    suspend fun deleteBookmark(urlHash: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM BookmarkEntity WHERE urlHash = :urlHash)")
    fun observeBookmarkStatus(urlHash: Long): Flow<Boolean>

    @Query("SELECT * FROM BookmarkEntity ORDER BY updatedAt DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>
}