package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.PersonEntity
import world.respect.datalayer.db.school.entities.PersonEntityWithRoles
import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.libutil.util.time.systemTimeInMillis

@Dao
interface PersonEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(personEntity: PersonEntity)

    @Query("""
        SELECT PersonEntity.pLastModified
          FROM PersonEntity
         WHERE PersonEntity.pGuidHash = :guidHash
         LIMIT 1
    """)
    suspend fun getLastModifiedByGuid(guidHash: Long): Long?


    @Transaction
    @Query("""
        SELECT * 
         FROM PersonEntity
        WHERE pUsername = :username
    """)
    suspend fun findByUsername(username: String): PersonEntityWithRoles?

    @Transaction
    @Query("""
       SELECT * 
         FROM PersonEntity
        WHERE pGuidHash = :guidHash
    """)
    suspend fun findByGuidNum(guidHash: Long): PersonEntityWithRoles?

    @Transaction
    @Query("""
        SELECT * 
         FROM PersonEntity
        WHERE pGuidHash = :guidHash
    """)
    fun findByGuidHashAsFlow(guidHash: Long): Flow<PersonEntityWithRoles?>

    @Query("""
        SELECT PersonEntity.pGuid AS guid, 
               PersonEntity.pGivenName AS givenName, 
               PersonEntity.pFamilyName AS familyName, 
               PersonEntity.pUsername AS username
          FROM PersonEntity
    """)
    fun findAllListDetailsAsFlow(): Flow<List<PersonListDetails>>

    @Transaction
    @Query("""
        SELECT * 
         FROM PersonEntity
    """)
    fun findAllAsFlow(): Flow<List<PersonEntityWithRoles>>

    @Query("""
        SELECT * 
         FROM PersonEntity
        WHERE PersonEntity.pStored > :since 
    """)
    suspend fun findAll(
        since: Long = 0,
    ): List<PersonEntityWithRoles>

    @Transaction
    @Query("""
        SELECT PersonEntity.*
          FROM PersonEntity
         WHERE PersonEntity.pGuidHash IN (:uidNums) 
    """)
    suspend fun findByUidList(uidNums: List<Long>): List<PersonEntityWithRoles>


    @Transaction
    @Query("""
        SELECT * 
         FROM PersonEntity
        WHERE PersonEntity.pStored > :since 
          AND (:guidHash = 0 OR PersonEntity.pGuidHash = :guidHash)
          AND (:inClazzGuidHash = 0 OR
               EXISTS(
                    SELECT EnrollmentEntity.eUid
                      FROM EnrollmentEntity
                     WHERE EnrollmentEntity.ePersonUidNum = PersonEntity.pGuidHash
                       AND EnrollmentEntity.eClassUidNum = :inClazzGuidHash
                       AND (:inClazzRoleFlag = 0 OR EnrollmentEntity.eRole = :inClazzRoleFlag)
                       AND :timeNow BETWEEN
                                    COALESCE(EnrollmentEntity.eBeginDate, 0) AND
                                    COALESCE(EnrollmentEntity.eEndDate, ${Long.MAX_VALUE})         
               )
              ) 
         AND (:filterByName IS NULL 
              OR (PersonEntity.pGivenName || ' ' || PersonEntity.pFamilyName) LIKE ('%' || :filterByName || '%'))
     ORDER BY PersonEntity.pGivenName
    """)
    fun findAllAsPagingSource(
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
    ): PagingSource<Int, PersonEntityWithRoles>

    @Query("""
        SELECT PersonEntity.pGuid AS guid, 
               PersonEntity.pGivenName AS givenName, 
               PersonEntity.pFamilyName AS familyName, 
               PersonEntity.pUsername AS username
          FROM PersonEntity
        WHERE PersonEntity.pStored > :since 
          AND (:guidHash = 0 OR PersonEntity.pGuidHash = :guidHash)
          AND (:inClazzGuidHash = 0 OR
               EXISTS(
                    SELECT EnrollmentEntity.eUid
                      FROM EnrollmentEntity
                     WHERE EnrollmentEntity.ePersonUidNum = PersonEntity.pGuidHash
                       AND EnrollmentEntity.eClassUidNum = :inClazzGuidHash
                       AND (:inClazzRoleFlag = 0 OR EnrollmentEntity.eRole = :inClazzRoleFlag)
                       AND :timeNow BETWEEN
                                    COALESCE(EnrollmentEntity.eBeginDate, 0) AND
                                    COALESCE(EnrollmentEntity.eEndDate, ${Long.MAX_VALUE})         
               )
              ) 
         AND (:filterByName IS NULL 
              OR (PersonEntity.pGivenName || ' ' || PersonEntity.pFamilyName) LIKE ('%' || :filterByName || '%'))
     ORDER BY PersonEntity.pGivenName
    """)
    fun findAllListDetailsAsPagingSource(
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
    ): PagingSource<Int, PersonListDetails>

    @Query("""
            SELECT * 
            FROM PersonEntity
            WHERE pGuid = :sourcedId
            """
    )
    suspend fun getAllUsers(sourcedId: String): List<PersonEntity>

    /**
     * Determines if the authenticated user (as per authenticatedUidNum) has permission to view the
     * person with the given uidNum
     *
     * @param authenticatedUidNum The uidNum of the authenticated user
     * @param uidNum The uidNum of the person the authenticated user wants to view
     */
    @Query("""
        SELECT (:authenticatedUidNum = :uidNum)
            OR EXISTS(
               SELECT SchoolPermissionGrantEntity.spgUidNum
                 FROM SchoolPermissionGrantEntity
                WHERE SchoolPermissionGrantEntity.spgToRole IN (
                      SELECT PersonRoleEntity.prRoleEnum
                        FROM PersonRoleEntity
                       WHERE PersonRoleEntity.prPersonGuidHash = :authenticatedUidNum)
                  AND (SchoolPermissionGrantEntity.spgPermissions & (
                      -- Select the required permission to view the given person based on the other persons role
                      CASE(
                        SELECT PersonRoleEntity.prRoleEnum
                          FROM PersonRoleEntity
                         WHERE PersonRoleEntity.prPersonGuidHash = :uidNum
                         LIMIT 1)
                         
                         WHEN ${PersonRoleEnum.SITE_ADMINISTRATOR_INT} THEN ${PermissionFlags.PERSON_ADMIN_READ}
                         WHEN ${PersonRoleEnum.SYSTEM_ADMINISTRATOR_INT} THEN ${PermissionFlags.PERSON_ADMIN_READ}
                         WHEN ${PersonRoleEnum.TEACHER_INT} THEN ${PermissionFlags.PERSON_TEACHER_READ}
                         WHEN ${PersonRoleEnum.STUDENT_INT} THEN ${PermissionFlags.PERSON_STUDENT_READ}
                         WHEN ${PersonRoleEnum.PARENT_INT} THEN ${PermissionFlags.PERSON_PARENT_READ}
                         ELSE ${Long.MAX_VALUE}
                      END
                  ) > 0))
               --Users can always read related persons (e.g. parent-child)   
            OR (:authenticatedUidNum IN 
                (SELECT PersonRelatedPersonEntity.prpOtherPersonUidNum
                   FROM PersonRelatedPersonEntity
                  WHERE PersonRelatedPersonEntity.prpPersonUidNum = :uidNum))       
    """)
    suspend fun userCanReadOther(
        authenticatedUidNum: Long,
        uidNum: Long,
    ): Boolean


}