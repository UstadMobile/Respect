package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toReportEntity
import world.respect.datalayer.db.school.adapters.toRespectReport
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.respect.model.RespectReport

class ReportDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
) : ReportDataSource {

    override suspend fun allReportsAsFlow(template: Boolean): Flow<DataLoadState<List<RespectReport>>> {
        return schoolDb.getReportEntityDao().getAllReportsByTemplate(template)
            .map { reportEntities ->
                DataReadyState(reportEntities.map { it.toRespectReport() })
            }
    }

    override suspend fun getReportAsync(
        loadParams: DataLoadParams,
        reportId: String
    ): DataLoadState<RespectReport> {
        val reportEntity = schoolDb.getReportEntityDao().getReportAsync(reportId)
        return if (reportEntity != null) {
            DataReadyState(reportEntity.toRespectReport())
        } else {
            NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
        }
    }

    override suspend fun getReportAsFlow(reportId: String): Flow<DataLoadState<RespectReport>> {
        return schoolDb.getReportEntityDao().getReportAsFlow(reportId).map { reportEntity ->
            if (reportEntity != null) {
                DataReadyState(reportEntity.toRespectReport())
            } else {
                NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
            }
        }
    }

    override suspend fun putReport(report: RespectReport) {
        val reportEntity = report.toReportEntity()
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                schoolDb.getReportEntityDao().putReport(reportEntity)
            }
        }
    }

    override suspend fun deleteReport(reportId: String) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                schoolDb.getReportEntityDao().deleteReportByUid(reportId)
            }
        }
    }
}