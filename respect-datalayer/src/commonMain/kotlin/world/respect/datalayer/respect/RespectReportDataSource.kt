package world.respect.datalayer.respect

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.respect.model.RespectReport

interface RespectReportDataSource {

    fun getAllReportsAsFlow(): Flow<DataLoadState<RespectReport>>

    fun getReportAsFlow(reportId: String): Flow<DataLoadState<RespectReport>>

    suspend fun putReport(report: RespectReport)

}