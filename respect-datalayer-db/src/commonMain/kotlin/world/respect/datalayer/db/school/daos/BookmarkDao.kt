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
         WHERE bPersonUid = :personUid
           AND bLearningUnitManifestUrl = :manifestUrl
    """)
    suspend fun updateStatus(
        personUid: String,
        manifestUrl: String,
        status: StatusEnum,
        lastModified: Instant
    )


    @Query("""
        SELECT EXISTS(
        SELECT 1 FROM BookmarkEntity
         WHERE bPersonUid = :personUid
           AND bLearningUnitManifestUrl = :manifestUrl
           AND bStatus = :activeStatus
    )
""")
    fun observeBookmarkStatus(
        personUid: String,
        manifestUrl: String,
        activeStatus: StatusEnum = StatusEnum.ACTIVE
    ): Flow<Boolean>

    @Query("""
        SELECT * FROM BookmarkEntity
         WHERE bPersonUid = :personUid
           AND bStatus = :activeStatus
      ORDER BY bLastModified DESC
   """)
    fun observeBookmarks(
        personUid: String,
        activeStatus: StatusEnum = StatusEnum.ACTIVE
    ): Flow<List<BookmarkEntity>>
}