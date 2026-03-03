package world.respect.datalayer.db.bookmarks.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.bookmarks.entities.BookmarkEntity
import world.respect.datalayer.school.model.StatusEnum

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("""
    UPDATE BookmarkEntity 
    SET status = :status, updatedAt = :updatedAt 
    WHERE urlHash = :urlHash
""")
    suspend fun updateBookmark(
        urlHash: Long,
        status: Int,
        updatedAt: Long = System.currentTimeMillis()
    )


    @Query("""
    SELECT EXISTS(
        SELECT 1 FROM BookmarkEntity 
        WHERE urlHash = :urlHash 
        AND status = :activeStatus
    )
""")
    fun getBookmarkStatus(
        urlHash: Long,
        activeStatus: Int = StatusEnum.ACTIVE.flag
    ): Flow<Boolean>


    @Query("""
    SELECT * FROM BookmarkEntity 
    WHERE status = :activeStatus
    ORDER BY updatedAt DESC
""")
    fun getAllBookmarks(
        activeStatus: Int = StatusEnum.ACTIVE.flag
    ): Flow<List<BookmarkEntity>>

}