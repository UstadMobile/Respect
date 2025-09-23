package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
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

    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: ReportDataSource.GetListParams,
        template: Boolean
    ): Flow<DataLoadState<List<Report>>> {
        return local.listAsFlow(loadParams, listParams, template)
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
                    timeQueued = timeNow,
                )
            }
        )
    }
}