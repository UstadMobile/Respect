package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.BookmarkEntity
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Instant
@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmark: BookmarkEntity)

    @Query("""
        UPDATE BookmarkEntity
           SET bStatus = :status,
               bLastModified = :lastModified
         WHERE bUidNum = :uidNum
    """)
    suspend fun updateStatus(
        uidNum: Long,
        status: StatusEnum,
        lastModified: Instant
    )

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM BookmarkEntity
             WHERE bUidNum = :uidNum
               AND bStatus = :activeStatus
        )
    """)
    fun observeBookmarkStatusByUid(
        uidNum: Long,
        activeStatus: StatusEnum = StatusEnum.ACTIVE
    ): Flow<Boolean>

    @Query("""
        SELECT * FROM BookmarkEntity
         WHERE bPersonUidNum = :personUidNum
           AND bStatus = :activeStatus
         ORDER BY bLastModified DESC
    """)
    fun observeBookmarks(
        personUidNum: Long,
        activeStatus: StatusEnum = StatusEnum.ACTIVE
    ): Flow<List<BookmarkEntity>>
}