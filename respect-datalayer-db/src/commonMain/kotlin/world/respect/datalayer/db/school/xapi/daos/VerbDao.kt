package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.VerbEntity

@Dao
interface VerbDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreAsync(entities: List<VerbEntity>)

    @Query(
        """
        SELECT VerbEntity.*
        FROM VerbEntity
        WHERE VerbEntity.verbUid = :uid 
    """
    )
    suspend fun findByUid(uid: Long): VerbEntity?

    @Query(
        """
        SELECT VerbEntity.*
        FROM VerbEntity
        WHERE VerbEntity.verbUrlId = :verbUrl 
    """
    )
    suspend fun findByVerbUrl(verbUrl: String): VerbEntity?
}