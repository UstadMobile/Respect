package world.respect.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

interface AssignmentDataSource: WritableDataSource<Assignment> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
    ) {

        companion object {

            fun fromParams(params: StringValues): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params)
                )
            }
        }

    }

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Assignment>>

    suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Assignment>

    fun listAsPagingSource(
        loadParams: DataLoadParams = DataLoadParams(),
        params: GetListParams = GetListParams(),
    ): IPagingSourceFactory<Int, Assignment>

    suspend fun list(
        loadParams: DataLoadParams = DataLoadParams(),
        params: GetListParams = GetListParams(),
    ): DataLoadState<List<Assignment>>

    override suspend fun store(list: List<Assignment>)

    companion object {

        const val ENDPOINT_NAME = "assignment"

    }
}