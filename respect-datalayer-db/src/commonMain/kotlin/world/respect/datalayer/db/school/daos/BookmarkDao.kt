package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.BookmarkEntity
import world.respect.datalayer.school.model.StatusEnum
/*

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query(
        """
        UPDATE BookmarkEntity 
           SET bStatus = :status, bUpdatedAt = :updatedAt 
         WHERE bPersonUidNum = :personUidNum
           AND bUrlHash = :urlHash
    """)
    suspend fun updateBookmark(
        personUidNum: Long,
        urlHash: Long,
        status: Int,
        updatedAt: Long = System.currentTimeMillis()
    )


    @Query(
        """
        SELECT EXISTS(
        SELECT 1 FROM BookmarkEntity 
                WHERE bPersonUidNum = :personUidNum
                  AND bUrlHash = :urlHash
                  AND bStatus = :activeStatus
       )
    """)
    fun getBookmarkStatus(
        personUidNum: Long,
        urlHash: Long,
        activeStatus: Int = StatusEnum.ACTIVE.flag
    ): Flow<Boolean>


    @Query("""
            SELECT * FROM BookmarkEntity 
                    WHERE bPersonUidNum = :personUidNum
                      AND bStatus = :activeStatus
                 ORDER BY bUpdatedAt DESC
    """)
    fun getAllBookmarks(
        personUidNum: Long,
        activeStatus: Int = StatusEnum.ACTIVE.flag
    ): Flow<List<BookmarkEntity>>
}*/


@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmark: BookmarkEntity)

    @Query("""
        UPDATE BookmarkEntity
           SET bStatus = :status,
               bUpdatedAt = :updatedAt
         WHERE bPersonUidNum = :personUidNum
           AND bUrlHash = :urlHash
    """)
    suspend fun updateStatus(
        personUidNum: Long,
        urlHash: Long,

        status: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM BookmarkEntity
             WHERE bPersonUidNum = :personUidNum
               AND bUrlHash = :urlHash
               AND (:includeDeleted = 1 OR bStatus = :activeStatus)
        )
    """)
    fun observeBookmarkStatus(
        personUidNum: Long,
        urlHash: Long,
        includeDeleted: Boolean = false,
        activeStatus: Int = StatusEnum.ACTIVE.flag
    ): Flow<Boolean>


    @Query("""
        SELECT * FROM BookmarkEntity
         WHERE bPersonUidNum = :personUidNum
           AND (:includeDeleted = 1 OR bStatus = :activeStatus)
         ORDER BY bUpdatedAt DESC
    """)
    fun observeBookmarks(
        personUidNum: Long,
        includeDeleted: Boolean = false,
        activeStatus: Int = StatusEnum.ACTIVE.flag
    ): Flow<List<BookmarkEntity>>
}