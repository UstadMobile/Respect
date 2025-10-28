package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.RepositoryPagingSourceFactory
import world.respect.datalayer.repository.shared.paging.loadAndUpdateLocal2
import world.respect.datalayer.school.AssignmentDataSource
import world.respect.datalayer.school.AssignmentDataSourceLocal
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.libutil.util.time.systemTimeInMillis

class AssignmentDataSourceRepository(
    override val local: AssignmentDataSourceLocal,
    override val remote: AssignmentDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : AssignmentDataSource, RepositoryModelDataSource<Assignment> {

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Assignment>> {
        return local.findByGuidAsFlow(guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Assignment> {
        local.updateFromRemoteIfNeeded(
            remote.findByGuid(params, guid), validationHelper
        )
        return local.findByGuid(params, guid)
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: AssignmentDataSource.GetListParams
    ): IPagingSourceFactory<Int, Assignment> {
        val remoteSource = remote.listAsPagingSource(loadParams, params).invoke()
        return RepositoryPagingSourceFactory(
            local = local.listAsPagingSource(loadParams, params),
            onRemoteLoad = { remoteLoadParams ->
                remoteSource.loadAndUpdateLocal2(
                    remoteLoadParams, local::updateLocal
                )
            },
            tag = "AssignmentRepo.listAsPagingSource"
        )
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: AssignmentDataSource.GetListParams
    ): DataLoadState<List<Assignment>> {
        local.updateFromRemoteListIfNeeded(
            remote.list(loadParams, params), validationHelper
        )
        return local.list(loadParams, params)
    }

    override suspend fun store(list: List<Assignment>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.ASSIGNMENT,
                    uid = it.uid,
                    timeQueued = timeNow,
                )
            }
        )
    }
}