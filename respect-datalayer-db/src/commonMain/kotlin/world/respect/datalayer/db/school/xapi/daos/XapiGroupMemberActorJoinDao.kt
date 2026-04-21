package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiGroupMemberActorJoin

@Dao
interface XapiGroupMemberActorJoinDao {


    @Query(
        """
        DELETE FROM XapiGroupMemberActorJoin
         WHERE XapiGroupMemberActorJoin.gmajGroupActorUid = :groupActorUid
    """
    )
    suspend fun deleteByGroupActorUidAsync(groupActorUid: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entities: List<XapiGroupMemberActorJoin>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListAsync(entities: List<XapiGroupMemberActorJoin>)

    @Query(
        """
        SELECT XapiGroupMemberActorJoin.*
          FROM XapiGroupMemberActorJoin
         WHERE XapiGroupMemberActorJoin.gmajGroupActorUid IN (:uidList)
    """
    )
    suspend fun findByGroupActorUidList(
        uidList: List<Long>
    ): List<XapiGroupMemberActorJoin>


}