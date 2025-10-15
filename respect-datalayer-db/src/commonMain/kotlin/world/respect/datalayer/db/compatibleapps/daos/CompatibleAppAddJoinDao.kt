package world.respect.datalayer.db.compatibleapps.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.compatibleapps.entities.CompatibleAppAddJoin

@Dao
interface CompatibleAppAddJoinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(compatibleAppAddJoin: CompatibleAppAddJoin)

    @Query("""
        SELECT * 
          FROM CompatibleAppAddJoin
         WHERE appCaeUid = :caeUid 
    """)
    fun getCompatibleAppAddJoinAsFlow(caeUid: Long): Flow<CompatibleAppAddJoin?>

}