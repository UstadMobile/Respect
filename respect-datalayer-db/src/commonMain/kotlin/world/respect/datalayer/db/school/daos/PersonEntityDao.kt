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
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.datalayer.school.model.StatusEnum
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.libutil.util.time.TimeConstants
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
           WITH $AUTHENTICATED_PERSON_RELATED_PERSON_UIDS_CTE_SQL,  
                $AUTHENTICATED_PERSON_CLASS_PERMISSIONS,
                $LIST_PERSONS_CTES_SQL
                
                         
         SELECT PersonEntity.*
           FROM PersonEntity
          WHERE PersonEntity.pGuidHash IN (
                SELECT DISTINCT uidNum
                  FROM AllPersons)
          ORDER BY PersonEntity.pGivenName
    """)
    fun listAsFlow(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        inClassOnDayInUtcMs: Long = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
        filterByPersonRole: Int = 0,
        includeRelated: Boolean = false,
        includeDeleted: Boolean = false,
    ): Flow<List<PersonEntityWithRoles>>

    @Query("""
           WITH $AUTHENTICATED_PERSON_RELATED_PERSON_UIDS_CTE_SQL,  
                $AUTHENTICATED_PERSON_CLASS_PERMISSIONS,
                $LIST_PERSONS_CTES_SQL
                         
         SELECT PersonEntity.*
           FROM PersonEntity
          WHERE PersonEntity.pGuidHash IN (
                SELECT DISTINCT uidNum
                  FROM AllPersons)
          ORDER BY PersonEntity.pGivenName
    """)
    suspend fun list(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        inClassOnDayInUtcMs: Long = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
        filterByPersonRole: Int = 0,
        includeRelated: Boolean = false,
        includeDeleted: Boolean = false,
    ): List<PersonEntityWithRoles>

    @Transaction
    @Query("""
        SELECT PersonEntity.*
          FROM PersonEntity
         WHERE PersonEntity.pGuidHash IN (:uidNums) 
    """)
    suspend fun findByUidList(uidNums: List<Long>): List<PersonEntityWithRoles>


    /**
     * @param inClassOnDayInUtcMs if filtering by clazzUid, and we want only those who have an active
     *        enrollment on a given day (e.g. today as per the users timezone), then we need to know
     *        what day that should be. LocalDate is stored as millis since epoch until 00:00 UTC for
     *        the given date. See the docs for startOfTodaysDateInMillisAtUtc function.
     */
    @Transaction
    @Query("""
           WITH $AUTHENTICATED_PERSON_RELATED_PERSON_UIDS_CTE_SQL,  
                $AUTHENTICATED_PERSON_CLASS_PERMISSIONS,
                $LIST_PERSONS_CTES_SQL
                         
         SELECT PersonEntity.*
           FROM PersonEntity
          WHERE PersonEntity.pGuidHash IN (
                SELECT DISTINCT uidNum
                  FROM AllPersons)
          ORDER BY PersonEntity.pGivenName
    """)
    fun listAsPagingSource(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClassOnDayInUtcMs: Long = 0,
        inClazzRoleFlag: Int = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
        filterByPersonRole: Int = 0,
        includeRelated: Boolean = false,
        includeDeleted: Boolean = false,
    ): PagingSource<Int, PersonEntityWithRoles>

    @Query("""
         WITH $AUTHENTICATED_PERSON_RELATED_PERSON_UIDS_CTE_SQL,  
                $AUTHENTICATED_PERSON_CLASS_PERMISSIONS,
                $LIST_PERSONS_CTES_SQL
        
        SELECT PersonEntity.pGuid AS guid, 
               PersonEntity.pGivenName AS givenName, 
               PersonEntity.pFamilyName AS familyName, 
               PersonEntity.pUsername AS username
          FROM PersonEntity
         WHERE PersonEntity.pGuidHash IN (
                SELECT DISTINCT uidNum
                  FROM AllPersons)
      ORDER BY PersonEntity.pGivenName
    """)
    fun findAllListDetailsAsPagingSource(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        inClassOnDayInUtcMs: Long = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
        filterByPersonRole: Int = 0,
        includeRelated: Boolean = false,
        includeDeleted: Boolean = false,
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



    companion object {


        /**
         * When expression that will evaluate to the permission flag required to read PersonEntity
         * based on the person's role.
         */
        const val READ_WHEN_CLAUSE_SQL = """
            CASE(SELECT PersonRoleEntity.prRoleEnum
                   FROM PersonRoleEntity
                  WHERE PersonRoleEntity.prPersonGuidHash = PersonEntity.pGuidHash
                  LIMIT 1)
                             
                 WHEN ${PersonRoleEnum.SITE_ADMINISTRATOR_INT} THEN ${PermissionFlags.PERSON_ADMIN_READ}
                 WHEN ${PersonRoleEnum.SYSTEM_ADMINISTRATOR_INT} THEN ${PermissionFlags.PERSON_ADMIN_READ}
                 WHEN ${PersonRoleEnum.TEACHER_INT} THEN ${PermissionFlags.PERSON_TEACHER_READ}
                 WHEN ${PersonRoleEnum.STUDENT_INT} THEN ${PermissionFlags.PERSON_STUDENT_READ}
                 WHEN ${PersonRoleEnum.PARENT_INT} THEN ${PermissionFlags.PERSON_PARENT_READ}
                 ELSE ${Long.MAX_VALUE}
            END     
        """

        const val AUTHENTICATED_PERSON_RELATED_PERSON_UIDS_CTE_SQL = """
            AuthenticatedPersonRelatedPersonUids(relatedPersonUidNum) AS (
                SELECT PersonRelatedPersonEntity.prpOtherPersonUidNum
                  FROM PersonRelatedPersonEntity
                 WHERE PersonRelatedPersonEntity.prpPersonUidNum = :authenticatedPersonUidNum)
        """


        /**
         * Find ClassPermissionsEntity that are available to the authenticatedPersonUidNum
         * including the permissions granted to the enrollment roles that the authenticated person
         * has, and, if the authenticated person is a parent, the roles that their children have,
         * such that a parent can see what their children can see.
         */
        const val AUTHENTICATED_PERSON_CLASS_PERMISSIONS = """
            AuthenticatedPersonClassPermissions AS (
                SELECT ClassPermissionEntity.*
                  FROM ClassPermissionEntity
                 WHERE (ClassPermissionEntity.cpeToEnrollmentRole, ClassPermissionEntity.cpeClassUidNum) IN 
                       (SELECT EnrollmentEntity.eRole, EnrollmentEntity.eClassUidNum
                          FROM EnrollmentEntity
                         WHERE EnrollmentEntity.ePersonUidNum IN (
                               SELECT :authenticatedPersonUidNum
                                UNION 
                               SELECT AuthenticatedPersonRelatedPersonUids.relatedPersonUidNum
                                 FROM AuthenticatedPersonRelatedPersonUids
                                WHERE ${PersonRoleEnum.PARENT_INT} IN 
                                      (SELECT PersonRoleEntity.prRoleEnum
                                         FROM PersonRoleEntity
                                        WHERE PersonRoleEntity.prPersonGuidHash = :authenticatedPersonUidNum))
                           AND EnrollmentEntity.eStatus = ${StatusEnum.ACTIVE_INT})
            )
        """



        /**
         * This CTE expression is shared between all functions that return a list. It handles the
         * includeRelated parameter efficiently. includeRelated is required by PersonDetail/Edit
         * to load related family members _and_ the ClassDetail screen to load related family
         * members for pending enrollees where applicable.
         */
        const val LIST_PERSONS_CTES_SQL = """
            Persons(uidNum) AS (
                   SELECT PersonEntity.pGuidHash 
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
                                   AND ((:includeDeleted OR :inClassOnDayInUtcMs = 0) 
                                        OR (     (:inClassOnDayInUtcMs >= COALESCE(EnrollmentEntity.eBeginDate, 0))
                                            AND ((:inClassOnDayInUtcMs - ${TimeConstants.DAY_IN_MILLIS - 1}) < COALESCE(EnrollmentEntity.eEndDate, ${Long.MAX_VALUE}))
                                            AND (:timeNow <= COALESCE(EnrollmentEntity.eRemovedAt, ${Long.MAX_VALUE}))
                                            AND EnrollmentEntity.eStatus = ${StatusEnum.ACTIVE_INT} ))         
                           ) 
                          ) 
                          -- Begin permission NOTE check should add permissions granted to children for parent
                      AND (
                           (PersonEntity.pGuidHash = :authenticatedPersonUidNum)
                        OR PersonEntity.pGuidHash IN 
                           (SELECT AuthenticatedPersonRelatedPersonUids.relatedPersonUidNum 
                              FROM AuthenticatedPersonRelatedPersonUids) 
                        OR EXISTS(
                               SELECT 1
                                 FROM SchoolPermissionGrantEntity
                                WHERE SchoolPermissionGrantEntity.spgToRole IN 
                                      (SELECT PersonRoleEntity.prRoleEnum
                                         FROM PersonRoleEntity
                                        WHERE PersonRoleEntity.prPersonGuidHash = :authenticatedPersonUidNum)
                                  AND (SchoolPermissionGrantEntity.spgPermissions & ($READ_WHEN_CLAUSE_SQL)) > 0)
                        OR EXISTS(
                               SELECT 1
                                 FROM AuthenticatedPersonClassPermissions
                                WHERE AuthenticatedPersonClassPermissions.cpeClassUidNum IN 
                                      (SELECT EnrollmentEntity.eClassUidNum
                                         FROM EnrollmentEntity
                                        WHERE EnrollmentEntity.ePersonUidNum = PersonEntity.pGuidHash)
                                  AND (AuthenticatedPersonClassPermissions.cpePermissions & ($READ_WHEN_CLAUSE_SQL)) > 0)
                          )
                      AND (:filterByName IS NULL 
                           OR (PersonEntity.pGivenName || ' ' || PersonEntity.pFamilyName) LIKE ('%' || :filterByName || '%'))
                      AND (:filterByPersonRole = 0 OR :filterByPersonRole IN 
                           (SELECT PersonRoleEntity.prRoleEnum
                              FROM PersonRoleEntity
                             WHERE PersonRoleEntity.prPersonGuidHash = PersonEntity.pGuidHash))
                      AND (:includeDeleted OR PersonEntity.pStatus != ${PersonStatusEnum.TO_BE_DELETED_INT})       
            ),
                
            RelatedPersons(uidNum) AS (
                SELECT PersonEntity.pGuidHash
                  FROM PersonEntity
                 WHERE :includeRelated 
                   AND PersonEntity.pGuidHash IN(
                        SELECT DISTINCT PersonRelatedPersonEntity.prpOtherPersonUidNum
                          FROM PersonRelatedPersonEntity
                         WHERE PersonRelatedPersonEntity.prpPersonUidNum IN(
                               SELECT Persons.uidNum
                                 FROM Persons)
                       )
            ),
                
            AllPersons(uidNum) AS (
                SELECT uidNum 
                  FROM Persons
                 UNION
                 SELECT uidNum 
                   FROM RelatedPersons
            )
        """

    }
}