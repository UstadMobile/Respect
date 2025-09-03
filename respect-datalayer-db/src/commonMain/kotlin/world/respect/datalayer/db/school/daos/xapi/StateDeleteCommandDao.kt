package world.respect.datalayer.db.school.daos.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import world.respect.datalayer.db.school.entities.xapi.StateDeleteCommand

@Dao
interface StateDeleteCommandDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsync(deleteCommand: StateDeleteCommand)
}