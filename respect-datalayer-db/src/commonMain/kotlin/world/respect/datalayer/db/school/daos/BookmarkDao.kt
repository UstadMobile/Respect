package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.adapters.BookmarkEntities
import world.respect.datalayer.db.school.entities.BookmarkEntity
import world.respect.datalayer.school.model.StatusEnum

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bookmarks: List<BookmarkEntity>)

    @Query(
        """
        SELECT EXISTS(
        SELECT 1 FROM BookmarkEntity
         WHERE bPersonUid = :personUid
           AND bUrl = :url
           AND bStatus = :status
         )
    """
    )
    fun getBookmarkStatus(
        personUid: String,
        url: String,
        status: StatusEnum = StatusEnum.ACTIVE
    ): Flow<Boolean>

    @Transaction
    @Query("""
        SELECT *
          FROM BookmarkEntity
         WHERE bPersonUid = :personUid
           AND (:includeDeleted OR bStatus = :activeStatus)
      ORDER BY bLastModified ASC
    """)
    suspend fun list(
        personUid: String,
        includeDeleted: Boolean = false,
        activeStatus: StatusEnum = StatusEnum.ACTIVE
    ): List<BookmarkEntities>

    @Transaction
    @Query("""
        SELECT *
          FROM BookmarkEntity
         WHERE bPersonUid = :personUid
           AND (:includeDeleted OR bStatus = :activeStatus)
      ORDER BY bLastModified ASC
    """)
     fun listAsFlow(
        personUid: String,
        includeDeleted: Boolean = false,
        activeStatus: StatusEnum = StatusEnum.ACTIVE
    ): Flow<List<BookmarkEntities>>

    @Transaction
    @Query("""
        SELECT *
          FROM BookmarkEntity
         WHERE bPersonUid = :personUid
           AND (:includeDeleted OR bStatus = :activeStatus)
      ORDER BY bLastModified ASC
    """)
    fun listAsPagingSource(
        personUid: String,
        includeDeleted: Boolean = false,
        activeStatus: StatusEnum = StatusEnum.ACTIVE
    ): PagingSource<Int,BookmarkEntities>

    @Query("""
        SELECT * 
          FROM BookmarkEntity 
         WHERE bPersonUid = :personUid
         AND NOT EXISTS ( 
                 SELECT 1
                   FROM OpdsPublicationEntity 
                  WHERE opeUrlHash = bUrlHash
             )
    """)
    suspend fun findBookmarksWithMissingPublication(
        personUid: String
    ): List<BookmarkEntities>

    @Query("""
        SELECT bLastModified
          FROM BookmarkEntity
         WHERE bPersonUid = :personUid
           AND bUrlHash = :urlHash
    """)
    suspend fun getBookmarkLastModified(
        personUid: String,
        urlHash: Long
    ): Long?

    @Query("""
        SELECT *
          FROM BookmarkEntity
         WHERE bUrlHash IN (:uids)
    """)
    suspend fun findByUidList(
        uids: List<Long>
    ): List<BookmarkEntities>
}