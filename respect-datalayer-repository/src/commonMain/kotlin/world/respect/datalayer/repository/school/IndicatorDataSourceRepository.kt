package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.ext.combineWithRemote
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.IndicatorDataSourceLocal
import world.respect.datalayer.school.model.Indicator

class IndicatorDataSourceRepository(
    private val local: IndicatorDataSourceLocal,
    private val remote: IndicatorDataSource,
) : IndicatorDataSource {
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
        if (remote is DataReadyState) {
            local.updateLocalFromRemote(listOf(remote.data))
        }

        return local.findByGuid(params, guid)
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Indicator>> {
        val remoteFlow = remote.findByGuidAsFlow(guid).onEach {
            if (it is DataReadyState) {
                local.updateLocalFromRemote(listOf(it.data))
            }
        }

        return local.findByGuidAsFlow(guid).combineWithRemote(remoteFlow)
    }

    override suspend fun store(indicator: Indicator) {
        local.store(indicator)
    }

    override suspend fun initializeDefaultIndicators(idGenerator: () -> String) {
        local.initializeDefaultIndicators(idGenerator)
    }
}