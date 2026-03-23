package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.SchoolConfigSettingEntity

@Dao
interface SchoolConfigSettingEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SchoolConfigSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entities: List<SchoolConfigSettingEntity>)

    @Query("""
        SELECT SchoolConfigSettingEntity.*
          FROM SchoolConfigSettingEntity
         WHERE scsKey = :key
         LIMIT 1
    """)
    suspend fun findByKey(key: String): SchoolConfigSettingEntity?

    @Query("""
        SELECT SchoolConfigSettingEntity.*
          FROM SchoolConfigSettingEntity
         WHERE scsKey = :key
    """)
    fun findByKeyAsFlow(key: String): Flow<SchoolConfigSettingEntity?>

    @Query("""
        SELECT SchoolConfigSettingEntity.*
          FROM SchoolConfigSettingEntity
         WHERE scsKey IN (:keys)
    """)
    suspend fun findByKeys(keys: List<String>): List<SchoolConfigSettingEntity>

    @Query("""
        SELECT SchoolConfigSettingEntity.*
          FROM SchoolConfigSettingEntity
         WHERE ((:key IS NULL) OR scsKey = :key)
           AND ((:since = 0) OR (scsStored > :since))
    """)
    fun listAsFlow(
        key: String? = null,
        since: Long = 0,
    ): Flow<List<SchoolConfigSettingEntity>>

    @Query("""
        SELECT SchoolConfigSettingEntity.*
          FROM SchoolConfigSettingEntity
         WHERE ((:key IS NULL) OR scsKey = :key)
           AND ((:since = 0) OR (scsStored > :since))
    """)
    suspend fun list(
        key: String? = null,
        since: Long = 0,
    ): List<SchoolConfigSettingEntity>

    @Query("""
        SELECT SchoolConfigSettingEntity.*
          FROM SchoolConfigSettingEntity
         WHERE ((:key IS NULL) OR scsKey = :key)
           AND ((:since = 0) OR (scsStored > :since))
    """)
    fun listAsPagingSource(
        key: String? = null,
        since: Long = 0,
    ): PagingSource<Int, SchoolConfigSettingEntity>

    @Query("""
        SELECT scsLastModified
          FROM SchoolConfigSettingEntity
         WHERE scsKey = :key
    """)
    suspend fun getLastModifiedByKey(key: String): Long?

}
