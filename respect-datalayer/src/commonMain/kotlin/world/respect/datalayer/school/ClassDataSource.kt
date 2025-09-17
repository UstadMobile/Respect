package world.respect.datalayer.school

import androidx.paging.PagingSource
import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.params.GetListCommonParams

interface ClassDataSource: WritableDataSource<Clazz> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
    ) {
        companion object {

            fun fromParams(params: StringValues) : GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params)
                )
            }

        }
    }

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Clazz>>

    suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Clazz>

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: GetListParams,
    ): PagingSource<Int, Clazz>

    override suspend fun store(
        list: List<Clazz>,
    )


    companion object {

        const val ENDPOINT_NAME = "class"

    }
}