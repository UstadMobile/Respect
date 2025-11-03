package world.respect.datalayer.db.school.daos.xapi


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.xapi.StatementEntity
import world.respect.datalayer.school.model.report.StatementReportRow


@Dao
interface StatementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entityList: List<StatementEntity>)

    @Query("SELECT * From StatementEntity LIMIT 1")
    fun getOneStatement(): Flow<StatementEntity?>

    @Query("SELECT * FROM StatementEntity")
    suspend fun getAll(): List<StatementEntity>

    @RawQuery
    suspend fun runReportQuery(query: RoomRawQuery): List<StatementReportRow>

}