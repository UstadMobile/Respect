package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.ClassEntity

@Dao
interface ClassEntityDao {

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


    @Query(LIST_SQL)
    fun findAllAsPagingSource(
        since: Long = 0,
        guidHash: Long = 0,
        code: String? = null,
    ): PagingSource<Int, ClassEntity>


    @Query(LIST_SQL)
    suspend fun list(
        since: Long = 0,
        guidHash: Long = 0,
        code: String? = null,
    ): List<ClassEntity>

    @Query("""
        SELECT ClassEntity.*
          FROM ClassEntity
         WHERE ClassEntity.cGuidHash in (:uids) 
    """)
    suspend fun findByUidList(uids: List<Long>) : List<ClassEntity>

    @Query("""
        SELECT ClassEntity.*
          FROM ClassEntity
         WHERE ClassEntity.cStudentInviteCode = :code
            OR ClassEntity.cTeacherInviteCode = :code
    """)
    suspend fun findByInviteCode(code: String): ClassEntity?


    companion object {

        const val LIST_SQL = """
       SELECT ClassEntity.* 
         FROM ClassEntity
        WHERE ClassEntity.cStored > :since 
          AND (:guidHash = 0 OR ClassEntity.cGuidHash = :guidHash)
          AND (:code IS NULL 
                OR ClassEntity.cStudentInviteCode = :code
                OR ClassEntity.cTeacherInviteCode = :code)
     ORDER BY ClassEntity.cTitle
        """

    }
}