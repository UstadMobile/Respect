package world.respect.server.routes.school.respect

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.AssignmentDataSource
import world.respect.server.util.ext.offsetLimitPagingLoadParams
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondOffsetLimitPaging

fun Route.AssignmentRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    }
) {
    get(AssignmentDataSource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        call.respondOffsetLimitPaging(
            params = call.request.queryParameters.offsetLimitPagingLoadParams(),
            pagingSource = schoolDataSource(call).assignmentDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                params = AssignmentDataSource.GetListParams.fromParams(call.request.queryParameters)
            ).invoke()
        )

    }
}