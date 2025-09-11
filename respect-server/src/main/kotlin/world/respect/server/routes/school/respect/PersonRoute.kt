package world.respect.server.routes.school.respect

import androidx.paging.PagingSource
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import world.respect.datalayer.DataLayerParams
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.PersonDataSource
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondOffsetLimitPaging

fun Route.PersonRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(PersonDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        val getListParams = PersonDataSource.GetListParams.fromParams(
            call.request.queryParameters
        )

        val loadParams = PagingSource.LoadParams.Refresh(
            key = call.request.queryParameters[DataLayerParams.OFFSET]?.toInt() ?: 0,
            loadSize = call.request.queryParameters[DataLayerParams.LIMIT]?.toInt() ?: 1000,
            placeholdersEnabled = false
        )

        call.respondOffsetLimitPaging(
            params = loadParams,
            pagingSource = schoolDataSource.personDataSource.listAsPagingSource(
                DataLoadParams(), getListParams
            )
        )
    }

}