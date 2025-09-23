package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.IndicatorDataSourceLocal
import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.libutil.util.time.systemTimeInMillis

class IndicatorDataSourceRepository(
    override val local: IndicatorDataSourceLocal,
    override val remote: IndicatorDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : IndicatorDataSource, RepositoryModelDataSource<Indicator> {

    override fun listAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String?
    ): Flow<DataLoadState<List<Indicator>>> {
        return local.listAsFlow(loadParams, searchQuery)
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Indicator> {
        val remote = remote.findByGuid(params, guid)
        local.updateFromRemoteIfNeeded(
            remote, validationHelper
        )

        return local.findByGuid(params, guid)
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Indicator>> {
        return local.findByGuidAsFlow(guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun initializeDefaultIndicators(idGenerator: () -> String) {
        local.initializeDefaultIndicators(idGenerator)
    }

    override suspend fun store(list: List<Indicator>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.CLASS,
                    uid = it.indicatorId,
                    timeQueued = timeNow,
                )
            }
        )
    }
}