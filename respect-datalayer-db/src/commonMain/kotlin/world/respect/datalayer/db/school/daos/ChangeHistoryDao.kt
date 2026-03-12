package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import world.respect.datalayer.db.school.entities.ChangeHistoryChangeEntity
import world.respect.datalayer.db.school.entities.ChangeHistoryEntity

@Dao
interface ChangeHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(
        entity: ChangeHistoryEntity
    )


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChanges(
        entities: List<ChangeHistoryChangeEntity>
    )

    @Transaction
    suspend fun insertHistoryWithChanges(
        history: ChangeHistoryEntity,
        changes: List<ChangeHistoryChangeEntity>
    ) {
        insertHistory(history)
        insertChanges(changes)
    }


}