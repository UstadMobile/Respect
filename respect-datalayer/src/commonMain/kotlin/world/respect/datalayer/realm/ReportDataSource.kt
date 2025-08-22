package world.respect.datalayer.realm

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.respect.model.RespectReport

interface ReportDataSource {

    /**
     * @param template if true, get list of templates. Otherwise list of reports for the active user
     */
    fun allReportsAsFlow(
        template: Boolean
    ): Flow<DataLoadState<List<RespectReport>>>

    fun getReportAsync(reportId: String): RespectReport?

    fun getReportAsFlow(reportId: String): Flow<DataLoadState<RespectReport>>

    fun putReport(report: RespectReport)

}
