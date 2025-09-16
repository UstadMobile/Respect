package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.adapters.EnrollmentEntities
import world.respect.datalayer.db.school.entities.EnrollmentEntity

@Dao
interface EnrollmentEntityDao {

    @Query("""
        SELECT EnrollmentEntity.*, 
               $SELECT_PERSON_AND_CLASS_UID  
          FROM EnrollmentEntity
         WHERE EnrollmentEntity.eUidNum = :uidNum
    """)
    suspend fun findByGuid(uidNum: Long): EnrollmentEntities?


    @Query("""
        SELECT EnrollmentEntity.*, 
               $SELECT_PERSON_AND_CLASS_UID  
          FROM EnrollmentEntity
         WHERE EnrollmentEntity.eUidNum = :uidNum
    """)
    fun findByGuidAsFlow(uidNum: Long): Flow<EnrollmentEntities?>

    @Query("""
        SELECT EnrollmentEntity.*, 
               $SELECT_PERSON_AND_CLASS_UID  
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
    ): PagingSource<Int, EnrollmentEntities>


    @Query("""
        SELECT EnrollmentEntity.eLastModified
          FROM EnrollmentEntity
         WHERE EnrollmentEntity.eUidNum = :uidNum 
    """)
    suspend fun getLastModifiedByUidNum(uidNum: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(enrolments: List<EnrollmentEntity>)

    companion object {

        const val SELECT_PERSON_AND_CLASS_UID  = """
            (SELECT ClassEntity.cGuid 
               FROM ClassEntity 
              WHERE ClassEntity.cGuidHash = EnrollmentEntity.eClassUidNum) AS classUid,
           (SELECT PersonEntity.pGuid
              FROM PersonEntity
             WHERE PersonEntity.pGuidHash = EnrollmentEntity.ePersonUidNum) AS personUid
        """

    }
}