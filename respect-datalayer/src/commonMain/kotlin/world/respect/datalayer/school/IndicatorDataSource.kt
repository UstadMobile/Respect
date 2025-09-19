package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.shared.params.GetListCommonParams

interface IndicatorDataSource {
    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
    )

    fun listAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String? = null,
    ): Flow<DataLoadState<List<Indicator>>>

    suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Indicator>

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Indicator>>

    suspend fun store(indicator: Indicator)

    suspend fun initializeDefaultIndicators(idGenerator: () -> String)

    companion object {
        const val ENDPOINT_NAME = "indicator"
    }
}