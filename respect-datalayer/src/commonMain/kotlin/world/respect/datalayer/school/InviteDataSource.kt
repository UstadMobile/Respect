package world.respect.datalayer.school

import io.ktor.util.StringValues
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.school.model.InviteStatusEnum
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

interface InviteDataSource : WritableDataSource<Invite> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val inviteCode:String? = null,
        val inviteRequired: Boolean? = null,
        val inviteStatus: InviteStatusEnum? = null,
        ) {
        companion object {
            fun fromParams(stringValues: StringValues): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(stringValues),
                    inviteCode = stringValues[PARAM_NAME_INVITE_CODE],
                )
            }
        }
    }

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: GetListParams,
    ): IPagingSourceFactory<Int, Invite>
    suspend fun findByGuid(guid: String): DataLoadState<Invite>
    suspend fun findByCode(code: String): DataLoadState<Invite>

    override suspend fun store(list: List<Invite>)

    companion object {
        const val ENDPOINT_NAME = "invite"
        const val PARAM_NAME_INVITE_CODE = "inviteCode"

    }
}
