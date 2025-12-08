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
import world.respect.datalayer.school.model.PermissionFlags

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
    fun listAsPagingSource(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        guidHash: Long = 0,
        code: String? = null,
    ): PagingSource<Int, ClassEntityWithPermissions>


    @Query(LIST_SQL)
    @Transaction
    suspend fun list(
        authenticatedPersonUidNum: Long,
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

    @Query("""
        SELECT ClassEntity.*
          FROM ClassEntity
         WHERE ClassEntity.cStudentInviteCode = :code
            OR ClassEntity.cTeacherInviteCode = :code 
    """)
    suspend fun findByInviteCode(code: String): List<ClassEntityWithPermissions>



    companion object {

        const val LIST_SQL = """
        WITH AuthenticatedUserEnrollments AS (
             SELECT EnrollmentEntity.*
               FROM EnrollmentEntity
              WHERE EnrollmentEntity.ePersonUidNum = :authenticatedPersonUidNum 
        )
            
            
       SELECT ClassEntity.* 
         FROM ClassEntity
        WHERE ClassEntity.cStored > :since 
          AND (:guidHash = 0 OR ClassEntity.cGuidHash = :guidHash)
          -- begin permission check
          AND (    EXISTS(
                   SELECT 1
                     FROM SchoolPermissionGrantEntity
                    WHERE SchoolPermissionGrantEntity.spgToRole IN (
                          SELECT PersonRoleEntity.prRoleEnum
                            FROM PersonRoleEntity
                           WHERE PersonRoleEntity.prPersonGuidHash = :authenticatedPersonUidNum) 
                      AND (SchoolPermissionGrantEntity.spgPermissions & ${PermissionFlags.CLASS_READ}) = ${PermissionFlags.CLASS_READ})
                OR EXISTS(
                   SELECT 1
                     FROM ClassPermissionEntity
                    WHERE ClassPermissionEntity.cpeClassUidNum = ClassEntity.cGuidHash
                      AND ClassPermissionEntity.cpeToEnrollmentRole IN 
                          (SELECT AuthenticatedUserEnrollments.eRole
                             FROM AuthenticatedUserEnrollments
                            WHERE AuthenticatedUserEnrollments.eClassUidNum = ClassEntity.cGuidHash)
                      AND (ClassPermissionEntity.cpePermissions & ${PermissionFlags.CLASS_READ}) = ${PermissionFlags.CLASS_READ})
          )
          -- end permission check
          
          AND (:code IS NULL 
                OR ClassEntity.cStudentInviteCode = :code
                OR ClassEntity.cTeacherInviteCode = :code)
     ORDER BY ClassEntity.cTitle
        """

    }
}