package world.respect.datalayer.db.school.daos.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.entities.xapi.GroupMemberActorJoin

@Dao
interface GroupMemberActorJoinDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entities: List<GroupMemberActorJoin>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListAsync(entities: List<GroupMemberActorJoin>)

    @Query(
        """
        UPDATE GroupMemberActorJoin
           SET gmajLastMod = :lastModTime
         WHERE gmajGroupActorUid = :gmajGroupActorUid
           AND gmajMemberActorUid = :gmajMemberActorUid
           AND gmajLastMod != :lastModTime 
    """
    )
    suspend fun updateLastModifiedTimeIfNeededAsync(
        gmajGroupActorUid: Long,
        gmajMemberActorUid: Long,
        lastModTime: Long
    )
}