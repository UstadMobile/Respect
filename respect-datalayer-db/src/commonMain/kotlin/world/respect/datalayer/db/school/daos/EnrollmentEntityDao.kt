package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.EnrollmentEntity

@Dao
interface EnrollmentEntityDao {

    @Query("""
        SELECT EnrollmentEntity.*
          FROM EnrollmentEntity
         WHERE EnrollmentEntity.eUidNum = :uidNum
    """)
    suspend fun findByGuid(uidNum: Long): EnrollmentEntity?


    @Query("""
        SELECT EnrollmentEntity.*
          FROM EnrollmentEntity
         WHERE EnrollmentEntity.eUidNum = :uidNum
    """)
    fun findByGuidAsFlow(uidNum: Long): Flow<EnrollmentEntity?>

    @Query("""
        SELECT EnrollmentEntity.*
          FROM EnrollmentEntity
         WHERE (:since <= 0 OR EnrollmentEntity.eStored > :since)
           AND (:uidNum = 0 OR EnrollmentEntity.eUidNum = :uidNum)
           AND (:classUidNum = 0 OR EnrollmentEntity.eClassUidNum = :classUidNum)
           AND (:classUidRoleFlag = 0 OR EnrollmentEntity.eRole = :classUidRoleFlag)
           AND (:personUidNum = 0 OR EnrollmentEntity.ePersonUidNum = :personUidNum)
    """)
    fun listAsPagingSource(
        since: Long = 0,
        uidNum: Long = 0,
        classUidNum: Long = 0,
        classUidRoleFlag: Int = 0,
        personUidNum: Long = 0,
    ): PagingSource<Int, EnrollmentEntity>


    @Query("""
        SELECT EnrollmentEntity.eLastModified
          FROM EnrollmentEntity
         WHERE EnrollmentEntity.eUidNum = :uidNum 
    """)
    suspend fun getLastModifiedByUidNum(uidNum: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(enrolments: List<EnrollmentEntity>)

    @Query("""
        SELECT EnrollmentEntity.*
          FROM EnrollmentEntity
         WHERE EnrollmentEntity.eUidNum IN (:uidNums) 
    """)
    suspend fun findByUidNumList(
        uidNums: List<Long>
    ): List<EnrollmentEntity>

    @Query("""
        DELETE FROM EnrollmentEntity 
        WHERE eUid = :uid
    """)
    suspend fun deleteEnrollment(uid: String)

}