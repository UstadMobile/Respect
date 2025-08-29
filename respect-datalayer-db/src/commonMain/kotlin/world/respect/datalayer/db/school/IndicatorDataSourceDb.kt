package world.respect.datalayer.db.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toIndicator
import world.respect.datalayer.db.school.adapters.toIndicatorEntity
import world.respect.datalayer.respect.model.Indicator
import world.respect.datalayer.school.IndicatorDataSource

class IndicatorDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
): IndicatorDataSource {

    override suspend fun allIndicatorAsFlow(): Flow<DataLoadState<List<Indicator>>> {
        return schoolDb.getIndicatorEntityDao().getAllIndicator().map { indicatorEntities ->
            DataReadyState(indicatorEntities.map { it.toIndicator() })
        }
    }

    override suspend fun getIndicatorAsync(
        loadParams: DataLoadParams,
        indicatorId: String
    ): DataLoadState<Indicator> {
        val indicatorEntity = schoolDb.getIndicatorEntityDao().getIndicatorAsync(indicatorId)
        return if (indicatorEntity != null) {
            DataReadyState(indicatorEntity.toIndicator())
        } else {
            NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
        }
    }

    override suspend fun getIndicatorAsFlow(indicatorId: String): Flow<DataLoadState<Indicator>> {
        return schoolDb.getIndicatorEntityDao().getIndicatorAsFlow(indicatorId).map { indicatorEntity ->
            if (indicatorEntity != null) {
                DataReadyState(indicatorEntity.toIndicator())
            } else {
                NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
            }
        }
    }

    override suspend fun putIndicator(indicator: Indicator) {
        val indicatorEntity = indicator.toIndicatorEntity()
        schoolDb.getIndicatorEntityDao().putIndicator(indicatorEntity)
    }

    override suspend fun updateIndicator(indicator: Indicator) {
        val indicatorEntity = indicator.toIndicatorEntity()
        schoolDb.getIndicatorEntityDao().updateIndicator(indicatorEntity)
    }
}