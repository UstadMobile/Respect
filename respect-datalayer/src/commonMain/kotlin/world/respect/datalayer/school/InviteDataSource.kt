package world.respect.datalayer.school

import io.ktor.util.StringValues
import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.params.GetListCommonParams

interface InviteDataSource : WritableDataSource<Invite> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams()
    ) {
        companion object {
            fun fromParams(stringValues: StringValues): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(stringValues),
                )
            }
        }
    }

    override suspend fun store(list: List<Invite>)

    companion object {
        const val ENDPOINT_NAME = "invite"
    }
}
