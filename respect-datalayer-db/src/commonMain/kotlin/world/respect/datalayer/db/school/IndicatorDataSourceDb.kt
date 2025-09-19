package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toIndicator
import world.respect.datalayer.db.school.adapters.toIndicatorEntity
import world.respect.datalayer.school.IndicatorDataSourceLocal
import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.school.model.report.DefaultIndicators
import world.respect.libxxhash.XXStringHasher
import kotlin.time.Clock

class IndicatorDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val xxStringHasher: XXStringHasher,
) : IndicatorDataSourceLocal {

    private suspend fun upsertIndicator(
        indicator: List<Indicator>,
        @Suppress("unused") forceOverwrite: Boolean
    ) {
        if (indicator.isEmpty()) return

        schoolDb.useWriterConnection { con ->
            val timeStored = Clock.System.now()
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                indicator.map { it.copy(stored = timeStored) }.forEach { clazz ->
                    val entities = clazz.toIndicatorEntity(xxStringHasher)
                    schoolDb.getIndicatorEntityDao().putIndicator(entities)
                }
            }
        }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String?
    ): Flow<DataLoadState<List<Indicator>>> {
        return schoolDb.getIndicatorEntityDao().getAllIndicator().map { list ->
            DataReadyState(
                data = list.map {
                    it.toIndicator()
                }
            )
        }
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Indicator> {
        return schoolDb.getIndicatorEntityDao().findByGuidHash(xxStringHasher.hash(guid))
            ?.toIndicator()?.let { DataReadyState(it) } ?: NoDataLoadedState.notFound()
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Indicator>> {
        return schoolDb.getIndicatorEntityDao().getIndicatorAsFlow(
            xxStringHasher.hash(guid)
        ).map { iEntity ->
            if (iEntity != null) {
                DataReadyState(
                    data = iEntity.toIndicator()
                )
            } else {
                NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
            }
        }
    }

    override suspend fun store(indicator: Indicator) {
        upsertIndicator(listOf(indicator), false)
    }

    override suspend fun initializeDefaultIndicators(idGenerator: () -> String) {
        val existingCount = schoolDb.getIndicatorEntityDao().getIndicatorCount()
        if (existingCount == 0) {
            DefaultIndicators.list.forEach { indicator ->
                val indicatorWithId = indicator.copy(
                    indicatorId = idGenerator()
                )
                store(indicatorWithId)
            }
        }
    }

    override suspend fun updateLocalFromRemote(
        list: List<Indicator>,
        forceOverwrite: Boolean
    ) {
        upsertIndicator(list, false)
    }
}