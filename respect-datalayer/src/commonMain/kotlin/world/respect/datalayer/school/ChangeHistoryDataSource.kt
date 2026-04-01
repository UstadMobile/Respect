package world.respect.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLayerParams
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.school.model.ChangeHistoryTableEnum
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

interface ChangeHistoryDataSource : WritableDataSource<ChangeHistoryEntry> {
    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val filterByTable: ChangeHistoryTableEnum? = null,
        val filterByWhoGuid: String? = null,
        val sinceTimestamp: Long? = null,
    ) {

        companion object {

            fun fromParams(stringValues: StringValues): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(stringValues),

                    filterByTable = stringValues[DataLayerParams.FILTER_BY_TABLE]?.let {
                        ChangeHistoryTableEnum.fromValue(it)
                    },

                    filterByWhoGuid = stringValues[DataLayerParams.FILTER_BY_WHO_GUID],
                    )
            }
        }
    }

    suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<List<ChangeHistoryEntry>>

    fun findByGuidAsFlow(
        guid: String
    ): Flow<DataLoadState<List<ChangeHistoryEntry>>>

    suspend fun list(
        loadParams: DataLoadParams = DataLoadParams(),
        params: GetListParams = GetListParams(),
    ): DataLoadState<List<ChangeHistoryEntry>>

    fun listAsPagingSource(
        dataLoadParams: DataLoadParams,
        getListParams: GetListParams
    ): IPagingSourceFactory<Int, ChangeHistoryEntry>

    suspend fun markSentToServer(
        changeHistoryEntries : List<ChangeHistoryEntry>
    )

    companion object {

        const val ENDPOINT_NAME = "changehistory"


    }
}