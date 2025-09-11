package world.respect.datalayer.school

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.shared.params.GetListCommonParams

interface ClassDataSource {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
    )

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Clazz>>

    suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Clazz>

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: GetListParams,
    ): PagingSource<Int, Clazz>

    suspend fun store(
        classes: List<Clazz>,
    )


    companion object {

        const val ENDPOINT_NAME = "class"

    }
}