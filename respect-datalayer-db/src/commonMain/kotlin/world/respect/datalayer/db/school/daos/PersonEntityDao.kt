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
import world.respect.datalayer.school.model.StatusEnum
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.libutil.util.time.TimeConstants
import world.respect.libutil.util.time.startOfTodaysDateInMillisAtUtc
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
           WITH $LIST_PERSONS_CTES_SQL
                         
         SELECT PersonEntity.*
           FROM PersonEntity
          WHERE PersonEntity.pGuidHash IN (
                SELECT DISTINCT uidNum
                  FROM AllPersons)
          ORDER BY PersonEntity.pGivenName
    """)
    fun listAsFlow(
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
        startOfTodaysDateInMillisAtUtc: Long = startOfTodaysDateInMillisAtUtc(),
        filterByPersonRole: Int = 0,
        includeRelated: Boolean = false,
        includeDeleted: Boolean = false,
    ): Flow<List<PersonEntityWithRoles>>

    @Query("""
           WITH $LIST_PERSONS_CTES_SQL
                         
         SELECT PersonEntity.*
           FROM PersonEntity
          WHERE PersonEntity.pGuidHash IN (
                SELECT DISTINCT uidNum
                  FROM AllPersons)
          ORDER BY PersonEntity.pGivenName
    """)
    suspend fun list(
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
        startOfTodaysDateInMillisAtUtc: Long = startOfTodaysDateInMillisAtUtc(),
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
     * @param startOfTodaysDateInMillisAtUtc we need to know the start of today as per the query
     *        (eg todays date as per the users timezone) in millis since epoch until 00:00 UTC to
     *        compare against start date and end date for enrollment if specified. See docs for
     *        startOfTodaysDateInMillisAtUtc function.
     */
    @Transaction
    @Query("""
           WITH $LIST_PERSONS_CTES_SQL
                         
         SELECT PersonEntity.*
           FROM PersonEntity
          WHERE PersonEntity.pGuidHash IN (
                SELECT DISTINCT uidNum
                  FROM AllPersons)
          ORDER BY PersonEntity.pGivenName
    """)
    fun listAsPagingSource(
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        filterByName: String? = null,
        startOfTodaysDateInMillisAtUtc: Long = startOfTodaysDateInMillisAtUtc(),
        timeNow: Long = systemTimeInMillis(),
        filterByPersonRole: Int = 0,
        includeRelated: Boolean = false,
        includeDeleted: Boolean = false,
    ): PagingSource<Int, PersonEntityWithRoles>

    @Query("""
         WITH $LIST_PERSONS_CTES_SQL 
        
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
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
        startOfTodaysDateInMillisAtUtc: Long = startOfTodaysDateInMillisAtUtc(),
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


    companion object {

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
                                   AND (:includeDeleted 
                                        OR (     (:startOfTodaysDateInMillisAtUtc >= COALESCE(EnrollmentEntity.eBeginDate, 0))
                                            AND ((:startOfTodaysDateInMillisAtUtc - ${TimeConstants.DAY_IN_MILLIS - 1}) < COALESCE(EnrollmentEntity.eEndDate, ${Long.MAX_VALUE}))
                                            AND (:timeNow <= COALESCE(EnrollmentEntity.eRemovedAt, ${Long.MAX_VALUE}))
                                            AND EnrollmentEntity.eStatus = ${StatusEnum.ACTIVE_INT} ))         
                           ) 
                          ) 
                      AND (:filterByName IS NULL 
                           OR (PersonEntity.pGivenName || ' ' || PersonEntity.pFamilyName) LIKE ('%' || :filterByName || '%'))
                      AND (:filterByPersonRole = 0 OR :filterByPersonRole IN 
                           (SELECT PersonRoleEntity.prRoleEnum
                              FROM PersonRoleEntity
                             WHERE PersonRoleEntity.prPersonGuidHash = PersonEntity.pGuidHash))
                      AND (:includeDeleted OR PersonEntity.pStatus = ${StatusEnum.ACTIVE_INT})       
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