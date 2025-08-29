package world.respect.datalayer.db.oneroaster.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.oneroaster.entities.OneRosterUserEntity
import world.respect.datalayer.db.oneroster.entities.OneRosterClassEntity
import world.respect.datalayer.oneroster.composites.ClazzListDetails

@Dao
interface OneRoasterEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(oneRosterClassEntity: OneRosterClassEntity)

    @Query("""
        DELETE FROM OneRosterClassEntity
         WHERE classSourcedId = :sourcedId
    """)
    suspend fun deleteBySourcedId(sourcedId: String)


    @Query("SELECT * FROM OneRosterClassEntity")
    suspend fun getAllClasses(): List<OneRosterClassEntity>

    @Query(
        """
        SELECT * 
         FROM OneRosterClassEntity
        WHERE classSourcedId = :sourcedId
    """
    )
    suspend fun findClassBySourcedId(sourcedId: String): OneRosterClassEntity

    @Query(
        """
        SELECT * 
         FROM OneRosterClassEntity
        WHERE classSourcedId = :sourcedId
    """
    )
    fun findClassBySourcedIdAsFlow(sourcedId: String): Flow<OneRosterClassEntity?>

    @Query("""
        SELECT OneRosterClassEntity.classSourcedId AS sourcedId, 
               OneRosterClassEntity.classTitle AS title, 
               OneRosterClassEntity.classStatus AS status
          FROM OneRosterClassEntity
    """)
    fun findAllListDetailsAsFlow(): Flow<List<ClazzListDetails>>

    @Query("SELECT * FROM OneRosterUserEntity")
    suspend fun getAllUsers(): List<OneRosterUserEntity>
}
