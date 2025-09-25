package world.respect.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

interface IndicatorDataSource : WritableDataSource<Indicator> {
    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
    ) {
        companion object {

            fun fromParams(params: StringValues): IndicatorDataSource.GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params),
                )
            }
        }
    }

    fun listAsFlow(
        loadParams: DataLoadParams,
        searchQuery: String? = null,
    ): Flow<DataLoadState<List<Indicator>>>

    suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Indicator>

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: IndicatorDataSource.GetListParams,
    ): IPagingSourceFactory<Int, Indicator>

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Indicator>>

    override suspend fun store(
        list: List<Indicator>,
    )

    suspend fun initializeDefaultIndicators(idGenerator: () -> String)

    companion object {
        const val ENDPOINT_NAME = "indicator"
    }
}