package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.SchoolAppEntity

@Dao
interface SchoolAppEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(list: List<SchoolAppEntity>)

    @Query("""
        SELECT SchoolAppEntity.saLastModified
          FROM SchoolAppEntity
         WHERE SchoolAppEntity.saUidNum = :uidNum
    """)
    suspend fun getLastModifiedByUidNum(uidNum: Long): Long?

    @Query("""
        SELECT SchoolAppEntity.*
          FROM SchoolAppEntity
         WHERE SchoolAppEntity.saUidNum IN (:uidNumList)
    """)
    suspend fun findByUidNumList(uidNumList: List<Long>): List<SchoolAppEntity>

    @Query("""
        SELECT SchoolAppEntity.*
          FROM SchoolAppEntity 
         WHERE (:includeDeleted OR SchoolAppEntity.saStatus = 1) 
    """)
    suspend fun list(
        includeDeleted: Boolean = false,
    ): List<SchoolAppEntity>

    @Query("""
        SELECT SchoolAppEntity.*
          FROM SchoolAppEntity 
         WHERE (:includeDeleted OR SchoolAppEntity.saStatus = 1)  
    """)
    fun listAsPagingSource(
        includeDeleted: Boolean = false,
    ): PagingSource<Int, SchoolAppEntity>

    @Query("""
        SELECT SchoolAppEntity.*
          FROM SchoolAppEntity 
         WHERE (:includeDeleted OR SchoolAppEntity.saStatus = 1)  
    """)
    fun listAsFlow(
        includeDeleted: Boolean = false,
    ): Flow<List<SchoolAppEntity>>

}