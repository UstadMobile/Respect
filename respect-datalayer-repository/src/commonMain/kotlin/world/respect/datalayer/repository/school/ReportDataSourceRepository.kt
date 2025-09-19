package world.respect.datalayer.repository.school

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.PagingSourceMediatorStore
import world.respect.datalayer.repository.shared.paging.RepositoryOffsetLimitPagingSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.ReportDataSourceLocal
import world.respect.datalayer.school.model.Report
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.libutil.util.time.systemTimeInMillis

class ReportDataSourceRepository(
    override val local: ReportDataSourceLocal,
    override val remote: ReportDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : ReportDataSource, RepositoryModelDataSource<Report> {

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
            onUpdateLocalFromRemote = local::updateLocal,
        )
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Report> {
        val remote = remote.findByGuid(params, guid)
        local.updateFromRemoteIfNeeded(
            remote, validationHelper
        )

        return local.findByGuid(params, guid)
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Report>> {
        return local.findByGuidAsFlow(guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun delete(guid: String) {
        local.delete(guid)
    }

    override suspend fun store(list: List<Report>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.PERSON,
                    uid = it.guid,
                    timestamp = timeNow,
                )
            }
        )
    }
}