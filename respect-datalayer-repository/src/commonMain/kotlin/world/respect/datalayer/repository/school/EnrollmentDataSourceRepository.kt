package world.respect.datalayer.repository.school

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.DoorOffsetLimitRemoteMediator
import world.respect.datalayer.repository.shared.paging.PagingSourceMediatorStore
import world.respect.datalayer.repository.shared.paging.RepositoryOffsetLimitPagingSource
import world.respect.datalayer.repository.shared.paging.loadAndUpdateLocal
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.EnrollmentDataSourceLocal
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.libutil.util.time.systemTimeInMillis

class EnrollmentDataSourceRepository(
    override val local: EnrollmentDataSourceLocal,
    override val remote: EnrollmentDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : EnrollmentDataSource, RepositoryModelDataSource<Enrollment> {

    private val mediatorStore = PagingSourceMediatorStore()

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Enrollment> {
        local.updateFromRemoteIfNeeded(
            remoteLoad = remote.findByGuid(loadParams, guid),
            validationHelper = validationHelper,
        )

        return local.findByGuid(loadParams, guid)
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<Enrollment>> {
        return local.findByGuidAsFlow(loadParams, guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(loadParams, guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        listParams: EnrollmentDataSource.GetListParams
    ): PagingSource<Int, Enrollment> {
        return RepositoryOffsetLimitPagingSource(
            local = local.listAsPagingSource(loadParams, listParams),
            remoteMediator = mediatorStore.getOrCreateMediator(0) {
                DoorOffsetLimitRemoteMediator { offset, limit ->
                    remote.listAsPagingSource(loadParams, listParams).loadAndUpdateLocal(
                        offset, limit, local::updateLocal
                    )
                }
            }
        )
    }

    override suspend fun store(list: List<Enrollment>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.ENROLLMENT,
                    uid = it.uid,
                    timestamp = timeNow,
                )
            }
        )
    }
}