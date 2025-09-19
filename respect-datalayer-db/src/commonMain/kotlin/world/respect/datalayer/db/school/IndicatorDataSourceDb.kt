package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.IndicatorEntities
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.IndicatorDataSourceLocal
import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.school.model.report.DefaultIndicators
import kotlin.time.Clock

class IndicatorDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
) : IndicatorDataSourceLocal {

    private suspend fun upsertIndicator(
        indicator: List<Indicator>,
        @Suppress("unused") forceOverwrite: Boolean
    ) {
        if (indicator.isEmpty()) return

        schoolDb.useWriterConnection { con ->
            val timeStored = Clock.System.now()
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                indicator.map { it.copy(stored = timeStored) }.forEach { indicator ->
                    val entities = indicator.toEntities(uidNumberMapper)
                    schoolDb.getIndicatorEntityDao().putIndicator(entities.indicator)
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
                    IndicatorEntities(it).toModel()
                }
            )
        }
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Indicator> {
        return schoolDb.getIndicatorEntityDao().findByGuidHash(
            uidNumberMapper(guid)
        )?.let {
            DataReadyState(IndicatorEntities(it).toModel())
        } ?: NoDataLoadedState.notFound()
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Indicator>> {
        return schoolDb.getIndicatorEntityDao().getIndicatorAsFlow(
            uidNumberMapper(guid)
        ).map { indicatorEntity ->
            indicatorEntity?.let { IndicatorEntities(it) }?.toModel()?.let {
                DataReadyState(it)
            } ?: NoDataLoadedState.notFound()
        }
    }

    override suspend fun store(list: List<Indicator>) {
        upsertIndicator(list, false)
    }

    override suspend fun initializeDefaultIndicators(idGenerator: () -> String) {
        val existingCount = schoolDb.getIndicatorEntityDao().getIndicatorCount()
        if (existingCount == 0) {
            // Collect all indicators first, then store them in a single batch
            val indicatorsToStore = DefaultIndicators.list.map { indicator ->
                indicator.copy(
                    indicatorId = idGenerator()
                )
            }
            store(indicatorsToStore)
        }
    }

    override suspend fun updateLocal(
        list: List<Indicator>,
        forceOverwrite: Boolean
    ) {
        upsertIndicator(list, false)
    }

    override suspend fun findByUidList(uids: List<String>): List<Indicator> {
        TODO("Not yet implemented")
    }
}