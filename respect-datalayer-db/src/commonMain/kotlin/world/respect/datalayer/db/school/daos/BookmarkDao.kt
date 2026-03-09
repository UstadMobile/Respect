package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.BookmarkEntity
import world.respect.datalayer.school.model.StatusEnum

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmarks: List<BookmarkEntity>)


    @Query("""
        SELECT EXISTS(
        SELECT 1 FROM BookmarkEntity
         WHERE bPersonUid = :personUid
           AND bLearningUnitManifestUrl = :manifestUrl
           AND bStatus = :activeStatus
         )
    """)
    fun getBookmarkStatus(
        personUid: String,
        manifestUrl: String,
        activeStatus: StatusEnum = StatusEnum.ACTIVE
    ): Flow<Boolean>

    @Transaction
    @Query("""
        SELECT *
          FROM BookmarkEntity
         WHERE bPersonUid = :personUid
           AND (:includeDeleted OR bStatus = :activeStatus)
      ORDER BY bLastModified DESC
    """)
    suspend fun list(
        personUid: String,
        includeDeleted: Boolean = false,
        activeStatus: StatusEnum = StatusEnum.ACTIVE
    ): List<BookmarkEntity>

}