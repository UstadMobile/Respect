package world.respect.datalayer.school.opds

import io.ktor.http.Parameters
import io.ktor.http.Url
import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.lib.opds.model.Publication

interface OpdsPublicationDataSourceLocal: OpdsPublicationDataSource {

    data class GetListParams(
        val title: String = "",
    ) {

        companion object {
            fun fromParams(params: StringValues): GetListParams =
                GetListParams(title = params["title"].orEmpty())
        }
    }

    fun searchByTitleAsFlow(listParams: GetListParams): Flow<List<Url>>

    val publicationNetworkValidationHelper: BaseDataSourceValidationHelper

    suspend fun updateOpdsPublication(publication: DataReadyState<Publication>)

}