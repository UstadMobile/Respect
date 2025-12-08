package world.respect.datalayer.db.school.daos.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.entities.xapi.ActivityLangMapEntry

@Dao
interface ActivityLangMapEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(entities: List<ActivityLangMapEntry>)

    /**
     * Upsert the lang map entity for an interaction entity if the related interaction entity exists
     * The interaction entity might not exists if the Activity is already defined.
     */
    @Query(
        """
        INSERT OR REPLACE ${ActivityLangMapEntryDaoCommon.INTO_LANG_MAP_WHERE_INTERACTION_ENTITY_EXISTS}      
    """
    )
    suspend fun upsertIfInteractionEntityExists(
        almeActivityUid: Long,
        almeHash: Long,
        almePropName: String?,
        almeLangCode: String?,
        almeValue: String?,
        almeAieHash: Long,
        almeLastMod: Long,
    )

    @Query(
        """
        UPDATE ActivityLangMapEntry
           SET almeValue = :almeValue,
               almeLastMod = :almeLastMod
         WHERE almeActivityUid = :almeActivityUid
           AND almeHash = :almeHash
           AND almeValue != :almeValue       
    """
    )
    suspend fun updateIfChanged(
        almeActivityUid: Long,
        almeHash: Long,
        almeValue: String?,
        almeLastMod: Long,
    )

    @Query(
        """
        SELECT ActivityLangMapEntry.*
          FROM ActivityLangMapEntry
         WHERE ActivityLangMapEntry.almeActivityUid = :activityUid
    """
    )
    suspend fun findAllByActivityUid(activityUid: Long): List<ActivityLangMapEntry>
}