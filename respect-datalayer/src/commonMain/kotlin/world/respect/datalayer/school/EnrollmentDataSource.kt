package world.respect.datalayer.school

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.shared.params.GetListCommonParams

interface EnrollmentDataSource {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
    )

    suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String,
    ): DataLoadState<Enrollment>

    fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String,
    ): Flow<DataLoadState<Enrollment>>

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        listParams: GetListParams,
    ): PagingSource<Int, Enrollment>

    suspend fun store(
        list: List<Enrollment>
    )

}