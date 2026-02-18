package world.respect.datalayer.school.opds

import io.ktor.http.Url
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper
import world.respect.lib.opds.model.OpdsFeed

interface OpdsFeedDataSourceLocal: OpdsFeedDataSource, BaseDataSourceValidationHelper {

    suspend fun updateLocal(
        url: Url,
        dataLoadResult: DataReadyState<OpdsFeed>,
    )

}
