package world.respect.datalayer.db.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.opds.entities.BookmarkEntity

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBookmark(bookmark: BookmarkEntity)

    @Query("SELECT COALESCE(isBookmarked, 0) FROM BookmarkEntity WHERE urlHash = :urlHash")
    fun observeBookmarkStatus(urlHash: Long): Flow<Boolean>

    @Query("SELECT * FROM BookmarkEntity WHERE isBookmarked = 1 ORDER BY updatedAt DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>
}