package world.respect.datalayer.respect

import app.cash.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.respect.model.RespectReport

interface RespectReportDataSource {

    fun getAllReportsAsFlow(): Flow<DataLoadState<RespectReport>>

    fun getReportAsFlow(reportId: String): Flow<DataLoadState<RespectReport>>

    suspend fun putReport(report: RespectReport)

    fun getReportsPagingSource(): PagingSource<Int, RespectReport>

    fun getTemplateReportsPagingSource(): PagingSource<Int, RespectReport>

}