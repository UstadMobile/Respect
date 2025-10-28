package world.respect.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.SchoolApp
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

interface SchoolAppDataSource: WritableDataSource<SchoolApp> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val includeDeleted: Boolean = false,
    ) {
        companion object {
            fun fromParams(
                params: StringValues
            ) : GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params),
                    includeDeleted = params[INCLUDE_DELETED]?.toBoolean() ?: false,
                )
            }
        }
    }

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: GetListParams,
    ): IPagingSourceFactory<Int, SchoolApp>


    fun listAsFlow(
        loadParams: DataLoadParams = DataLoadParams(),
        params: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<SchoolApp>>>

    suspend fun list(
        loadParams: DataLoadParams,
        params: GetListParams,
    ): DataLoadState<List<SchoolApp>>


    companion object {

        const val ENDPOINT_NAME = "schoolappsetup"

        const val INCLUDE_DELETED = "includeDeleted"

    }

}