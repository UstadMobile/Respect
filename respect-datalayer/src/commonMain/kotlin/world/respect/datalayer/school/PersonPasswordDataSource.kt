package world.respect.datalayer.school

import io.ktor.http.Parameters
import world.respect.datalayer.DataLoadState
import world.respect.datalayer.school.model.PersonPassword
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.params.GetListCommonParams

interface PersonPasswordDataSource: WritableDataSource<PersonPassword> {

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
    ): DataLoadState<List<PersonPassword>>

    companion object {

        const val ENDPOINT_NAME = "PersonPassword"

    }

}