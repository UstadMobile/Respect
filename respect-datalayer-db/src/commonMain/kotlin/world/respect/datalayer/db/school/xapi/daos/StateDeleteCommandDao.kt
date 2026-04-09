package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import world.respect.datalayer.db.school.xapi.entities.StateDeleteCommand

@Dao
interface StateDeleteCommandDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsync(deleteCommand: StateDeleteCommand)
}