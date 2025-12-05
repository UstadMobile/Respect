package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.ClassEntity
import world.respect.datalayer.db.school.entities.ClassEntityWithPermissions

@Dao
interface ClassEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(classEntity: ClassEntity)

    @Transaction
    @Query("""
        SELECT * 
         FROM ClassEntity
        WHERE ClassEntity.cGuidHash = :guidHash
    """)
    fun findByGuidHashAsFlow(guidHash: Long): Flow<ClassEntityWithPermissions?>

    @Query("""
        SELECT * 
         FROM ClassEntity
        WHERE ClassEntity.cGuidHash = :guidHash
    """)
    suspend fun findByGuid(guidHash: Long): ClassEntityWithPermissions?


    @Query("""
        SELECT ClassEntity.cLastModified
          FROM ClassEntity
         WHERE ClassEntity.cGuidHash = :uidNum 
    """)
    suspend fun getLastModifiedByGuid(
        uidNum: Long
    ): Long?

    @Query(LIST_SQL)
    @Transaction
    fun findAllAsPagingSource(
        since: Long = 0,
        guidHash: Long = 0,
        code: String? = null,
    ): PagingSource<Int, ClassEntityWithPermissions>


    @Query(LIST_SQL)
    @Transaction
    suspend fun list(
        since: Long = 0,
        guidHash: Long = 0,
        code: String? = null,
    ): List<ClassEntityWithPermissions>

    @Query("""
        SELECT ClassEntity.*
          FROM ClassEntity
         WHERE ClassEntity.cGuidHash in (:uids) 
    """)
    suspend fun findByUidList(uids: List<Long>) : List<ClassEntityWithPermissions>


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