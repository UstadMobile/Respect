package world.respect.datalayer.repository.school


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.RepositoryPagingSourceFactory
import world.respect.datalayer.repository.shared.paging.loadAndUpdateLocal2
import world.respect.datalayer.school.ChangeHistoryDataSource
import world.respect.datalayer.school.ChangeHistoryLocal
import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.libutil.util.time.systemTimeInMillis

class ChangeHistoryDataSourceRepository(
    override val local: ChangeHistoryLocal,
    override val remote: ChangeHistoryDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : ChangeHistoryDataSource, RepositoryModelDataSource<ChangeHistoryEntry> {

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<List<ChangeHistoryEntry>>{

        local.updateFromRemoteListIfNeeded(
            remoteLoad = remote.findByGuid(loadParams, guid),
            validationHelper = validationHelper,
        )

        return local.findByGuid(loadParams, guid)
    }

    override fun findByGuidAsFlow(
        guid: String
    ): Flow<DataLoadState<List<ChangeHistoryEntry>>> {
        return local.findByGuidAsFlow(guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(guid).onEach {
                local.updateFromRemoteListIfNeeded(it, validationHelper)
            }
        )
    }
    override suspend fun list(
        loadParams: DataLoadParams,
        params: ChangeHistoryDataSource.GetListParams
    ): DataLoadState<List<ChangeHistoryEntry>> {
        local.updateFromRemoteListIfNeeded(
            remote.list(loadParams, params), validationHelper
        )
        return local.list(loadParams, params)
    }
    override fun listAsPagingSource(
        dataLoadParams: DataLoadParams,
        getListParams: ChangeHistoryDataSource.GetListParams
    ): IPagingSourceFactory<Int, ChangeHistoryEntry> {
        val remote = remote.listAsPagingSource(
            dataLoadParams = dataLoadParams,
            getListParams = getListParams.copy(common = getListParams.common.copy(includeDeleted = true)),
        ).invoke()

        return RepositoryPagingSourceFactory(
            local = local.listAsPagingSource(dataLoadParams, getListParams),
            onRemoteLoad = { remoteLoadParams ->
                remote.loadAndUpdateLocal2(
                    loadParams = remoteLoadParams,
                    onUpdateLocalFromRemote = local::updateLocal,
                )
            },
            tag = { "ChangeHistoryDataSourceRepo(listParams=$getListParams)" }
        )
    }

    override suspend fun markSentToServer(changeHistoryEntries: List<ChangeHistoryEntry>) {
        local.markSentToServer(changeHistoryEntries)
    }


    override suspend fun store(list: List<ChangeHistoryEntry>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.CHANGE_HISTORY,
                    uid = it.guid,
                    timeQueued = timeNow,
                )
            }
        )
    }
}
