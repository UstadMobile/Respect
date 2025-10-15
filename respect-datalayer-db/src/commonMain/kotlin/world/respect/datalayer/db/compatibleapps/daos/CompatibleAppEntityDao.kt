package world.respect.datalayer.db.compatibleapps.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.compatibleapps.entities.CompatibleAppEntity
import world.respect.datalayer.db.shared.LastModifiedAndETagDb

@Dao
interface CompatibleAppEntityDao {

    @Query("""
        SELECT CompatibleAppEntity.caeLastModified AS lastModified,
               CompatibleAppEntity.caeEtag AS etag
          FROM CompatibleAppEntity
         WHERE caeUid = :caeUid
    """)
    suspend fun getNetworkValidationInfo(caeUid: Long): LastModifiedAndETagDb?

    @Query("""SELECT * 
                        FROM CompatibleAppEntity 
                       WHERE caeUid = :caeUid""")
    suspend fun selectByUid(caeUid: Long): CompatibleAppEntity?

    @Query("SELECT * FROM CompatibleAppEntity")
    fun selectAllAsFlow(): Flow<List<CompatibleAppEntity>>

    @Query("""
        SELECT *
          FROM CompatibleAppEntity
               JOIN CompatibleAppAddJoin
                    ON CompatibleAppEntity.caeUid = CompatibleAppAddJoin.appCaeUid
         WHERE CompatibleAppAddJoin.added = 1
    """)
    fun selectAddedAppsAsFlow(): Flow<List<CompatibleAppEntity>>

    @Query("""SELECT * 
                        FROM CompatibleAppEntity 
                       WHERE caeUid = :caeUid""")
    fun selectByUidAsFlow(caeUid: Long): Flow<CompatibleAppEntity?>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun upsert(compatibleApps: List<CompatibleAppEntity>)

}