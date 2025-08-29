package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.respect.model.RespectReport

interface ReportDataSource {

    /**
     * @param template if true, get list of templates. Otherwise list of reports for the active user
     */
   suspend fun allReportsAsFlow(
        template: Boolean
    ): Flow<DataLoadState<List<RespectReport>>>

    suspend fun getReportAsync(loadParams: DataLoadParams, reportId: String): DataLoadState<RespectReport>

    suspend fun getReportAsFlow(reportId: String): Flow<DataLoadState<RespectReport>>

    suspend fun putReport(report: RespectReport)

    suspend fun deleteReport(reportId: String)


}
