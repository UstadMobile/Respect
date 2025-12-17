package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.EnrollmentEntity
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.school.model.StatusEnum
import world.respect.libutil.util.time.TimeConstants

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

    @Query(LIST_SQL)
    fun listAsPagingSource(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        uidNum: Long = 0,
        classUidNum: Long = 0,
        classUidRoleFlag: Int = 0,
        personUidNum: Long = 0,
        activeOnDayInUtcMs: Long = 0,
        notRemovedBefore: Long = 0,
        includeDeleted: Boolean = false,
    ): PagingSource<Int, EnrollmentEntity>

    @Query(LIST_SQL)
    suspend fun list(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        uidNum: Long = 0,
        classUidNum: Long = 0,
        classUidRoleFlag: Int = 0,
        personUidNum: Long = 0,
        activeOnDayInUtcMs: Long = 0,
        notRemovedBefore: Long = 0,
        includeDeleted: Boolean = false,
    ): List<EnrollmentEntity>

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


    companion object {

        const val REQUIRED_PERMISSION_EXPRESSION = """
            CASE(EnrollmentEntity.eRole)
            WHEN ${EnrollmentRoleEnum.STUDENT_FLAG} THEN ${PermissionFlags.PERSON_STUDENT_READ}
            WHEN ${EnrollmentRoleEnum.PENDING_STUDENT_FLAG} THEN ${PermissionFlags.PERSON_STUDENT_READ}
            WHEN ${EnrollmentRoleEnum.TEACHER_FLAG} THEN ${PermissionFlags.PERSON_TEACHER_READ}
            WHEN ${EnrollmentRoleEnum.PENDING_TEACHER_FLAG} THEN ${PermissionFlags.PERSON_TEACHER_READ}
            ELSE ${Long.MAX_VALUE}
            END
        """


        /**
         * Reading the enrollment entity requires the PERSON_STUDENT_READ permission: this can be
         * done via a SchoolPermissionGrant (school-wide) or ClassPermission (specific class only).
         */
        const val LIST_SQL = """
          WITH ${PersonEntityDao.AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL},  
               ${PersonEntityDao.AUTHENTICATED_PERSON_CLASS_PERMISSIONS}
            
        SELECT EnrollmentEntity.*
          FROM EnrollmentEntity
         WHERE (:since <= 0 OR EnrollmentEntity.eStored > :since)
           AND (:uidNum = 0 OR EnrollmentEntity.eUidNum = :uidNum)
           AND (:classUidNum = 0 OR EnrollmentEntity.eClassUidNum = :classUidNum)
           AND (:classUidRoleFlag = 0 OR EnrollmentEntity.eRole = :classUidRoleFlag)
           AND (:personUidNum = 0 OR EnrollmentEntity.ePersonUidNum = :personUidNum)
           AND (:includeDeleted OR EnrollmentEntity.eStatus = ${StatusEnum.ACTIVE_INT})
           AND (:activeOnDayInUtcMs = 0 
                OR (     (:activeOnDayInUtcMs >= COALESCE(EnrollmentEntity.eBeginDate, 0))
                    AND ((:activeOnDayInUtcMs - ${TimeConstants.DAY_IN_MILLIS - 1}) < COALESCE(EnrollmentEntity.eEndDate, ${Long.MAX_VALUE}))))
           AND (:notRemovedBefore = 0 OR EnrollmentEntity.eRemovedAt > :notRemovedBefore)
           AND (   EnrollmentEntity.ePersonUidNum IN 
                   (SELECT AuthenticatedPermissionPersonUids.uidNum
                      FROM AuthenticatedPermissionPersonUids) 
                OR EXISTS(
                     SELECT 1
                       FROM SchoolPermissionGrantEntity
                      WHERE SchoolPermissionGrantEntity.spgToRole IN 
                            (SELECT PersonRoleEntity.prRoleEnum
                               FROM PersonRoleEntity
                              WHERE PersonRoleEntity.prPersonGuidHash IN 
                                    (SELECT AuthenticatedPermissionPersonUids.uidNum
                                       FROM AuthenticatedPermissionPersonUids)
                                AND (SchoolPermissionGrantEntity.spgPermissions & ($REQUIRED_PERMISSION_EXPRESSION)) > 0))  
                OR EXISTS(
                     SELECT 1
                       FROM AuthenticatedPersonClassPermissions
                      WHERE AuthenticatedPersonClassPermissions.cpeClassUidNum = EnrollmentEntity.eClassUidNum
                        AND (AuthenticatedPersonClassPermissions.cpePermissions & ($REQUIRED_PERMISSION_EXPRESSION)) > 0)
               )      
        """

    }
}