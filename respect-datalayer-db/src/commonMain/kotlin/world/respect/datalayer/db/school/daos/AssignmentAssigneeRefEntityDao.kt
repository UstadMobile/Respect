package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.entities.AssignmentAssigneeRefEntity

@Dao
interface AssignmentAssigneeRefEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entities: List<AssignmentAssigneeRefEntity>)

    @Query("""
        DELETE FROM AssignmentAssigneeRefEntity
         WHERE aarAeUidNum = :uidNum
    """)
    suspend fun deleteByAssignmentUidNum(uidNum: Long)


}