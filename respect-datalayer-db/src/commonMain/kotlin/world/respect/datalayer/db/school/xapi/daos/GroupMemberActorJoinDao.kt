package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.GroupMemberActorJoin

@Dao
interface GroupMemberActorJoinDao {


    @Query("""
        DELETE FROM GroupMemberActorJoin
         WHERE GroupMemberActorJoin.gmajGroupActorUid = :groupActorUid
    """)
    suspend fun deleteByGroupActorUidAsync(groupActorUid: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entities: List<GroupMemberActorJoin>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListAsync(entities: List<GroupMemberActorJoin>)

}