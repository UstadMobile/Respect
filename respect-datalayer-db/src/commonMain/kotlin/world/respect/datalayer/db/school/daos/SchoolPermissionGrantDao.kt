package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.SchoolPermissionGrantEntity

@Dao
interface SchoolPermissionGrantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entities: List<SchoolPermissionGrantEntity>)

    @Query("""
        SELECT SchoolPermissionGrantEntity.*
          FROM SchoolPermissionGrantEntity
         WHERE (:uidNum = 0 OR SchoolPermissionGrantEntity.spgUidNum = :uidNum)
    """)
    suspend fun list(
        uidNum: Long
    ): List<SchoolPermissionGrantEntity>

    @Query("""
        SELECT SchoolPermissionGrantEntity.*
          FROM SchoolPermissionGrantEntity
         WHERE (:uidNum = 0 OR SchoolPermissionGrantEntity.spgUidNum = :uidNum)
    """)
    fun listAsPagingSource(
        uidNum: Long
    ): PagingSource<Int, SchoolPermissionGrantEntity>

    @Query("""
        SELECT SchoolPermissionGrantEntity.spgLastModified
          FROM SchoolPermissionGrantEntity
         WHERE SchoolPermissionGrantEntity.spgUidNum = :uidNum
    """)
    suspend fun getLastModifiedByUidNum(uidNum: Long): Long?

    @Query("""
        SELECT SchoolPermissionGrantEntity.*
          FROM SchoolPermissionGrantEntity
         WHERE SchoolPermissionGrantEntity.spgUidNum = :uidNum
    """)
    suspend fun findByUidNum(uidNum: Long): SchoolPermissionGrantEntity?

    @Query("""
        SELECT SchoolPermissionGrantEntity.*
          FROM SchoolPermissionGrantEntity
         WHERE SchoolPermissionGrantEntity.spgUidNum = :uidNum
    """)
    fun findByUidNumAsFlow(uidNum: Long): Flow<SchoolPermissionGrantEntity?>

    @Query("""
        SELECT SchoolPermissionGrantEntity.*
          FROM SchoolPermissionGrantEntity
         WHERE SchoolPermissionGrantEntity.spgUidNum IN (:uidNums)
    """)
    suspend fun findByUidNums(uidNums: List<Long>): List<SchoolPermissionGrantEntity>

}
