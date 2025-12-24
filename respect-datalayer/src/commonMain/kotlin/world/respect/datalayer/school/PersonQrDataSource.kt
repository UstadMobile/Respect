package world.respect.datalayer.school

import io.ktor.http.Parameters
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.PersonBadge
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.params.GetListCommonParams

interface PersonQrDataSource : WritableDataSource<PersonBadge> {
    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
    ) {
        companion object {
            fun fromParams(params: Parameters): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params)
                )
            }
        }
    }

    suspend fun listAll(
        listParams: GetListParams = GetListParams(),
    ): DataLoadState<List<PersonBadge>>

    fun listAllAsFlow(
        loadParams: DataLoadParams = DataLoadParams(),
        listParams: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<PersonBadge>>>

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<PersonBadge>>

    suspend fun deletePersonBadge(
        uidNum: Long
    )

    suspend fun existsByQrCodeUrl(
        url: String,
        uidNum: Long
    ): Boolean

    companion object {

        const val ENDPOINT_NAME = "personqrcode"

    }
}