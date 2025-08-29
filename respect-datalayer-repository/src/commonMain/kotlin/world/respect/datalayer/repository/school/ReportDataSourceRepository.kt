package world.respect.datalayer.repository.realm

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.respect.model.RespectReport

class ReportDataSourceRepository(
    private val remote: ReportDataSource,
) : ReportDataSource {
    override suspend fun allReportsAsFlow(template: Boolean): Flow<DataLoadState<List<RespectReport>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getReportAsync(
        loadParams: DataLoadParams,
        reportId: String
    ): DataLoadState<RespectReport> {
        TODO("Not yet implemented")
    }

    override suspend fun getReportAsFlow(reportId: String): Flow<DataLoadState<RespectReport>> {
        TODO("Not yet implemented")
    }

    override suspend fun putReport(report: RespectReport) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReport(reportId: String) {
        TODO("Not yet implemented")
    }
}