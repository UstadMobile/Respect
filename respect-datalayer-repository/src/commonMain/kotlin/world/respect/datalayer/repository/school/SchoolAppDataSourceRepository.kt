package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.RepositoryPagingSourceFactory
import world.respect.datalayer.school.SchoolAppDataSource
import world.respect.datalayer.school.SchoolAppDataSourceLocal
import world.respect.datalayer.school.model.SchoolApp
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.repository.shared.paging.loadAndUpdateLocal2
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.libutil.util.time.systemTimeInMillis


class SchoolAppDataSourceRepository(
    override val local: SchoolAppDataSourceLocal,
    override val remote: SchoolAppDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
): SchoolAppDataSource, RepositoryModelDataSource<SchoolApp> {

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: SchoolAppDataSource.GetListParams
    ): IPagingSourceFactory<Int, SchoolApp> {
        val remoteSource = remote.listAsPagingSource(
            loadParams, params.copy(includeDeleted = true)
        ).invoke()
        return RepositoryPagingSourceFactory(
            local = local.listAsPagingSource(loadParams, params),
            onRemoteLoad = { remoteLoadParams ->
                remoteSource.loadAndUpdateLocal2(
                    remoteLoadParams, local::updateLocal
                )
            },
            tag = { "SchoolAppDataSourceRepository" },
        )
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: SchoolAppDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolApp>>> {
        return local.listAsFlow(loadParams, params).combineWithRemote(
            remoteFlow = remote.listAsFlow(
                loadParams, params.copy(includeDeleted = true)
            ).onEach {
                local.updateFromRemoteListIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolAppDataSource.GetListParams
    ): DataLoadState<List<SchoolApp>> {
        local.updateFromRemoteListIfNeeded(
            remoteLoad = remote.list(loadParams, params.copy(includeDeleted = true)),
            validationHelper = validationHelper
        )
        return local.list(loadParams, params)
    }

    override suspend fun store(list: List<SchoolApp>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.SCHOOL_APP,
                    uid = it.uid,
                    timeQueued = timeNow,
                )
            }
        )
    }

}