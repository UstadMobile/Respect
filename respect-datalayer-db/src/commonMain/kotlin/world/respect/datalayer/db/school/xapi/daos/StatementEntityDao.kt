package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.xapi.entities.StatementEntity
import world.respect.datalayer.school.model.report.StatementReportRow

@Dao
interface StatementEntityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entityList: List<StatementEntity>)

    @Query("SELECT * From StatementEntity LIMIT 1")
    fun getOneStatement(): Flow<StatementEntity?>

    @Query("SELECT * FROM StatementEntity")
    suspend fun getAll(): List<StatementEntity>

    @RawQuery
    suspend fun runReportQuery(query: RoomRawQuery): List<StatementReportRow>

}