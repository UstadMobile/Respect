package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.LastModifiedAndPermission
import world.respect.datalayer.db.school.entities.SchoolConfigSettingEntity

@Dao
interface SchoolConfigSettingEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entities: List<SchoolConfigSettingEntity>)

    @Query(LIST_SQL)
    fun listAsFlow(
        authenticatedPersonUidNum: Long,
        keys: List<String>? = null,
        since: Long = 0,
    ): Flow<List<SchoolConfigSettingEntity>>

    @Query(LIST_SQL)
    suspend fun list(
        authenticatedPersonUidNum: Long,
        keys: List<String>? = null,
        since: Long = 0,
    ): List<SchoolConfigSettingEntity>

    @Query("""
        SELECT scsLastModified
          FROM SchoolConfigSettingEntity
         WHERE scsKey = :key
    """)
    suspend fun getLastModifiedByKey(key: String): Long?

    @Query(GET_LAST_MODIFIED_AND_HAS_PERMISSION_SQL)
    suspend fun getLastModifiedAndHasPermission(
        authenticatedPersonUidNum: Long,
        key: String,
        canWriteRolesMask: Int = 0
    ): LastModifiedAndPermission

    companion object {

        private const val AUTHENTICATED_USER_ROLE_SQL = """
            SELECT PersonRoleEntity.prRoleEnum
              FROM PersonRoleEntity
             WHERE PersonRoleEntity.prPersonGuidHash = :authenticatedPersonUidNum
             LIMIT 1
        """

        private const val READ_PERMISSION_CHECK_SQL = """
            scsAnonCanRead OR (($AUTHENTICATED_USER_ROLE_SQL) & scsCanReadFlags) > 0
        """

        private const val WRITE_PERMISSION_CHECK_SQL = """
            (($AUTHENTICATED_USER_ROLE_SQL) & scsCanWriteFlags) > 0
        """

        private const val LIST_SQL = """
            SELECT SchoolConfigSettingEntity.*
              FROM SchoolConfigSettingEntity
             WHERE scsKey IN (:keys)
             AND SchoolConfigSettingEntity.scsStored > :since
             AND ($READ_PERMISSION_CHECK_SQL)
        """

        private const val GET_LAST_MODIFIED_AND_HAS_PERMISSION_SQL = """
            SELECT 0 AS uidNum,
               (SELECT SchoolConfigSettingEntity.scsLastModified
                  FROM SchoolConfigSettingEntity
                 WHERE SchoolConfigSettingEntity.scsKey = :key) AS lastModified,
               (
                -- for existing records
                EXISTS (
                   SELECT 1
                     FROM SchoolConfigSettingEntity
                    WHERE SchoolConfigSettingEntity.scsKey = :key
                      AND ($WRITE_PERMISSION_CHECK_SQL)
                ) 
                OR 
                -- for new records (using the passed mask)
                (NOT EXISTS (SELECT 1 FROM SchoolConfigSettingEntity WHERE scsKey = :key) 
                 AND ($AUTHENTICATED_USER_ROLE_SQL) & :canWriteRolesMask > 0)
               ) AS hasPermission
        """
    }
}
