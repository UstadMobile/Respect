package world.respect.datalayer.repository.school


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.RepositoryPagingSourceFactory
import world.respect.datalayer.repository.shared.paging.loadAndUpdateLocal2
import world.respect.datalayer.school.ChangeHistoryDataSource
import world.respect.datalayer.school.ChangeHistoryLocal
import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory

class ChangeHistoryDataSourceRepository(
    override val local: ChangeHistoryLocal,
    override val remote: ChangeHistoryDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
) : ChangeHistoryDataSource, RepositoryModelDataSource<ChangeHistoryEntry> {

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<ChangeHistoryEntry> {
        local.updateFromRemoteIfNeeded(
            remoteLoad = remote.findByGuid(loadParams, guid),
            validationHelper = validationHelper,
        )

        return local.findByGuid(loadParams, guid)
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<ChangeHistoryEntry>> {
        return local.findByGuidAsFlow(loadParams, guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(loadParams, guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
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


    override suspend fun store(list: List<ChangeHistoryEntry>) {
    }
}
