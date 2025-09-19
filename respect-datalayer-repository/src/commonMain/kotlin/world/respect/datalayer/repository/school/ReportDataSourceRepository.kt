package world.respect.datalayer.repository.school

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.repository.shared.paging.PagingSourceMediatorStore
import world.respect.datalayer.repository.shared.paging.RepositoryOffsetLimitPagingSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.ReportDataSourceLocal
import world.respect.datalayer.school.model.Report

class ReportDataSourceRepository(
    private val local: ReportDataSourceLocal,
    private val remote: ReportDataSource
) : ReportDataSource {

    private val mediatorStore = PagingSourceMediatorStore()

    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: ReportDataSource.GetListParams,
        template: Boolean
    ): Flow<DataLoadState<List<Report>>> {
        return local.listAsFlow(loadParams, listParams, template)
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: ReportDataSource.GetListParams
    ): PagingSource<Int, Report> {
        return RepositoryOffsetLimitPagingSource(
            local = local.listAsPagingSource(loadParams, params),
            remote = remote.listAsPagingSource(loadParams, params),
            argKey = 0,
            mediatorStore = mediatorStore,
            onUpdateLocalFromRemote = local::updateLocalFromRemote,
        )
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Report> {
        val remote = remote.findByGuid(params, guid)
        if (remote is DataReadyState) {
            local.updateLocalFromRemote(listOf(remote.data))
        }

        return local.findByGuid(params, guid)
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Report>> {
        val remoteFlow = remote.findByGuidAsFlow(guid).onEach {
            if (it is DataReadyState) {
                local.updateLocalFromRemote(listOf(it.data))
            }
        }

        return local.findByGuidAsFlow(guid).combineWithRemote(remoteFlow)
    }

    override suspend fun store(report: Report) {
        local.store(report)
    }

    override suspend fun delete(guid: String) {
        local.delete(guid)
    }
}