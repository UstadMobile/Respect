package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.realm.entities.ReportEntity

@Dao
interface  ReportEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putReport(reportEntity: ReportEntity)

    @Query("""
        SELECT * 
        FROM ReportEntity
        WHERE reportIsTemplate = :template
    """)
    fun getAllReportsByTemplate(template: Boolean): Flow<List<ReportEntity>>

    @Query("""
        SELECT * 
         FROM ReportEntity
        WHERE reportId = :reportId
    """)
    suspend fun getReportAsync(reportId: String): ReportEntity?

    @Query("""
        SELECT * 
         FROM ReportEntity
        WHERE reportId = :reportId
    """)
    fun getReportAsFlow(reportId: String): Flow<ReportEntity?>

    @Query("DELETE FROM ReportEntity WHERE reportId = :reportUid")
    suspend fun deleteReportByUid(reportUid: String)
}