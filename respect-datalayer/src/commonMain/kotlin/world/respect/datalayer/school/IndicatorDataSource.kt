package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Indicator

interface IndicatorDataSource {

    suspend fun allIndicatorAsFlow(): Flow<DataLoadState<List<Indicator>>>

    suspend fun getIndicatorAsync(loadParams: DataLoadParams, indicatorId: String): DataLoadState<Indicator>

    suspend fun getIndicatorAsFlow(indicatorId: String): Flow<DataLoadState<Indicator>>

    suspend fun putIndicator(indicator: Indicator)

    suspend fun updateIndicator(indicator: Indicator)

    suspend fun initializeDefaultIndicators(idGenerator: () -> String)
}