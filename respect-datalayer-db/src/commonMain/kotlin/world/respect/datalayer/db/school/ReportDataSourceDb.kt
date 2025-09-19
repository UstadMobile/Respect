package world.respect.datalayer.db.school

import androidx.paging.PagingSource
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
import world.respect.datalayer.school.ReportDataSourceLocal
import world.respect.datalayer.school.model.Report
import world.respect.datalayer.shared.paging.map
import world.respect.libxxhash.XXStringHasher
import kotlin.time.Clock

class ReportDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val xxStringHasher: XXStringHasher,
) : ReportDataSourceLocal {

    private suspend fun upsertReports(
        reports: List<Report>,
        @Suppress("unused") forceOverwrite: Boolean
    ) {
        if (reports.isEmpty()) return

        schoolDb.useWriterConnection { con ->
            val timeStored = Clock.System.now()
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                reports.map { it.copy(stored = timeStored) }.forEach { clazz ->
                    val entities = clazz.toReportEntity(xxStringHasher)
                    schoolDb.getReportEntityDao().putReport(entities)
                }
            }
        }
    }

    override suspend fun updateLocalFromRemote(
        list: List<Report>,
        forceOverwrite: Boolean
    ) {
        upsertReports(list, false)
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: ReportDataSource.GetListParams,
        template: Boolean
    ): Flow<DataLoadState<List<Report>>> {
        return schoolDb.getReportEntityDao().getAllReportsByTemplate(template).map { list ->
            DataReadyState(
                data = list.map {
                    it.toRespectReport()
                }
            )
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: ReportDataSource.GetListParams
    ): PagingSource<Int, Report> {
        return schoolDb.getReportEntityDao().findAllAsPagingSource(
            since = params.common.since?.toEpochMilliseconds() ?: 0,
            guidHash = params.common.guid?.let { xxStringHasher.hash(it) } ?: 0,
        ).map {
            it.toRespectReport()
        }
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Report> {
        return schoolDb.getReportEntityDao().findByGuidHash(xxStringHasher.hash(guid))
            ?.toRespectReport()?.let { DataReadyState(it) } ?: NoDataLoadedState.notFound()
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Report>> {
        return schoolDb.getReportEntityDao().findByGuidHashAsFlow(
            xxStringHasher.hash(guid)
        ).map { reportEntity ->
            if (reportEntity != null) {
                DataReadyState(
                    data = reportEntity.toRespectReport()
                )
            } else {
                NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
            }
        }
    }

    override suspend fun store(report: Report) {
        upsertReports(listOf(report), false)
    }

    override suspend fun delete(guid: String) {
        return schoolDb.getReportEntityDao().deleteReportByGuidHash(
            xxStringHasher.hash(guid)
        )
    }
}