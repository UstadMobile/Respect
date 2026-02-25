package world.respect.datalayer.db.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.opds.entities.BookmarkEntity
import world.respect.datalayer.db.opds.entities.OpdsPublicationEntity

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBookmark(bookmark: BookmarkEntity)

    @Query("SELECT COALESCE(isBookmarked, 0) FROM BookmarkEntity WHERE urlHash = :urlHash")
    fun observeBookmarkStatus(urlHash: Long): Flow<Boolean>

    @Query("""
        SELECT OpdsPublicationEntity.* FROM OpdsPublicationEntity 
        INNER JOIN BookmarkEntity ON OpdsPublicationEntity.opeUrlHash = BookmarkEntity.urlHash
        WHERE BookmarkEntity.isBookmarked = 1
    """)
    fun getBookmarkedPublications(): Flow<List<OpdsPublicationEntity>>
}