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
        val inviteCode: String? = null,
    ) {
        companion object {

            fun fromParams(params: StringValues) : GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params),
                    inviteCode = params[PARAM_NAME_INVITE_CODE],
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

    suspend fun list(
        loadParams: DataLoadParams,
        params: GetListParams
    ): DataLoadState<List<Clazz>>

    override suspend fun store(
        list: List<Clazz>,
    )


    companion object {

        const val ENDPOINT_NAME = "class"

        const val PARAM_NAME_INVITE_CODE = "inviteCode"

    }
}