package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.ClassEntity

@Dao
interface ClazzEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(classEntity: ClassEntity)

    @Query("""
        SELECT * 
         FROM ClassEntity
        WHERE ClassEntity.cGuidHash = :guidHash
    """)
    fun findByGuidHashAsFlow(guidHash: Long): Flow<ClassEntity?>

    @Query("""
        SELECT * 
         FROM ClassEntity
        WHERE ClassEntity.cGuidHash = :guidHash
    """)
    suspend fun findByGuid(guidHash: Long): ClassEntity?


    @Query("""
        SELECT * 
         FROM ClassEntity
        WHERE ClassEntity.cStored > :since 
          AND (:guidHash = 0 OR ClassEntity.cGuidHash = :guidHash)
     ORDER BY ClassEntity.cTitle
    """)
    fun findAllAsPagingSource(
        since: Long = 0,
        guidHash: Long = 0,
    ): PagingSource<Int, ClassEntity>



    @Query("""
        SELECT ClassEntity.*
          FROM ClassEntity
         WHERE ClassEntity.cGuidHash in (:uids) 
    """)
    suspend fun findByUidList(uids: List<Long>) : List<ClassEntity>

}