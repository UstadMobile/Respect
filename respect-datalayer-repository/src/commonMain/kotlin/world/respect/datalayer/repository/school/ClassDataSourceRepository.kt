package world.respect.datalayer.repository.school

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.DoorOffsetLimitRemoteMediator
import world.respect.datalayer.repository.shared.paging.PagingSourceMediatorStore
import world.respect.datalayer.repository.shared.paging.RepositoryOffsetLimitPagingSource
import world.respect.datalayer.repository.shared.paging.loadAndUpdateLocal
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.ClassDataSourceLocal
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.libutil.util.time.systemTimeInMillis

class ClassDataSourceRepository(
    override val local: ClassDataSourceLocal,
    override val remote: ClassDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : ClassDataSource, RepositoryModelDataSource<Clazz> {

    private val mediatorStore = PagingSourceMediatorStore()

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Clazz>> {
        return local.findByGuidAsFlow(guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Clazz> {
        local.updateFromRemoteIfNeeded(
            remote.findByGuid(params, guid), validationHelper
        )
        return local.findByGuid(params, guid)
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: ClassDataSource.GetListParams
    ): PagingSource<Int, Clazz> {
        return RepositoryOffsetLimitPagingSource(
            local = local.listAsPagingSource(loadParams, params),
            remoteMediator = mediatorStore.getOrCreateMediator(0) {
                DoorOffsetLimitRemoteMediator { offset, limit ->
                    remote.listAsPagingSource(loadParams, params).loadAndUpdateLocal(
                        offset, limit, local::updateLocal
                    )
                }
            },
        )
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: ClassDataSource.GetListParams
    ): DataLoadState<List<Clazz>> {
        local.updateFromRemoteListIfNeeded(
            remote.list(loadParams, params), validationHelper
        )
        return local.list(loadParams, params)
    }

    override suspend fun store(list: List<Clazz>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.CLASS,
                    uid = it.guid,
                    timeQueued = timeNow,
                )
            }
        )
    }
}