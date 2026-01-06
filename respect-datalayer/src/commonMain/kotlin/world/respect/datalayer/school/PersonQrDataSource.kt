package world.respect.datalayer.school

import io.ktor.http.Parameters
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.SchoolAppDataSource.Companion.INCLUDE_DELETED
import world.respect.datalayer.school.model.PersonBadge
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.params.GetListCommonParams

interface PersonQrDataSource : WritableDataSource<PersonBadge> {
    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val includeDeleted: Boolean = false
    ) {
        companion object {
            fun fromParams(params: Parameters): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params),
                    includeDeleted = params[INCLUDE_DELETED]?.toBoolean() ?: false
                )
            }
        }
    }

    suspend fun listAll(
        loadParams: DataLoadParams,
        listParams: GetListParams = GetListParams(),
    ): DataLoadState<List<PersonBadge>>

    fun listAllAsFlow(
        loadParams: DataLoadParams = DataLoadParams(),
        listParams: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<PersonBadge>>>

    fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<PersonBadge>>


    suspend fun existsByQrCodeUrl(
        url: String,
        uidNum: Long
    ): Boolean

    companion object {

        const val ENDPOINT_NAME = "personqrcode"

    }
}