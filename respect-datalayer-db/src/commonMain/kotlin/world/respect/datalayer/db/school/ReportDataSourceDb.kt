package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.ReportEntities
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.ReportDataSourceLocal
import world.respect.datalayer.school.model.Report
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import kotlin.collections.map
import kotlin.time.Clock

class ReportDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
) : ReportDataSourceLocal {

    private suspend fun upsertReports(
        reports: List<Report>,
        forceOverwrite: Boolean = false,
    ) {
        if(reports.isEmpty())
            return

        schoolDb.useWriterConnection { con ->
            val timeStored = Clock.System.now()

            var numUpdated = 0
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                reports.map { it.copy(stored = timeStored) }.forEach { report ->
                    val entities = report.toEntities(uidNumberMapper)
                    val lastModifiedInDb = schoolDb.getReportEntityDao().getLastModifiedByGuid(
                        entities.reportEntity.rGuidHash
                    ) ?: -1

                    if(forceOverwrite ||
                        entities.reportEntity.rLastModified > lastModifiedInDb) {
                        schoolDb.getReportEntityDao().putReport(entities.reportEntity)
                        numUpdated++
                    }
                }
            }
            Napier.d("RPaging/ReportDataSourceDb: updated: $numUpdated/${reports.size}")

        }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: ReportDataSource.GetListParams,
        template: Boolean
    ): Flow<DataLoadState<List<Report>>> {
        return schoolDb.getReportEntityDao().getAllReportsByTemplate(template).map { list ->
            DataReadyState(
                data = list.map {
                    ReportEntities(it).toModel()
                }
            )
        }
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Report> {
        return schoolDb.getReportEntityDao().findByGuidHash(
            uidNumberMapper(guid)
        )?.let {
            DataReadyState(ReportEntities(it).toModel())
        } ?: NoDataLoadedState.notFound()
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: ReportDataSource.GetListParams
    ): IPagingSourceFactory<Int, Report> {
        return IPagingSourceFactory {
            schoolDb.getReportEntityDao().findAllAsPagingSource(
                since = params.common.since?.toEpochMilliseconds() ?: 0,
                guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
            ).map {
                ReportEntities(it).toModel()
            }
        }
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Report>> {
        return schoolDb.getReportEntityDao().findByGuidHashAsFlow(
            uidNumberMapper(guid)
        ).map { reportEntity ->
            if (reportEntity != null) {
                DataReadyState(
                    data = ReportEntities(reportEntity).toModel()
                )
            } else {
                NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
            }
        }
    }

    override suspend fun delete(guid: String) {
        return schoolDb.getReportEntityDao().deleteReportByGuidHash(
            uidNumberMapper(guid)
        )
    }

    override suspend fun store(list: List<Report>) {
        upsertReports(list, false)
    }

    override suspend fun updateLocal(
        list: List<Report>,
        forceOverwrite: Boolean
    ) {
        upsertReports(list, false)
    }

    override suspend fun findByUidList(uids: List<String>): List<Report> {
        TODO("Not yet implemented")
    }
}