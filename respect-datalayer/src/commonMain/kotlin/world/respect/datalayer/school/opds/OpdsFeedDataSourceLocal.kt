package world.respect.datalayer.school.opds

import io.ktor.http.Url
import world.respect.datalayer.DataReadyState
import world.respect.lib.opds.model.OpdsFeed

interface OpdsFeedDataSourceLocal: OpdsFeedDataSource {

    suspend fun updateLocal(
        url: Url,
        dataLoadResult: DataReadyState<OpdsFeed>,
    )

}
