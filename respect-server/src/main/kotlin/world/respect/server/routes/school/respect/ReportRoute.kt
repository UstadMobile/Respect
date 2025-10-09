package world.respect.server.routes.school.respect

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.model.Report
import world.respect.server.util.ext.offsetLimitPagingLoadParams
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondOffsetLimitPaging

@Suppress("FunctionName")
fun Route.ReportRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {

    get(ReportDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        val getListParams = ReportDataSource.GetListParams.fromParams(call.request.queryParameters)

        val loadParams = call.request.queryParameters.offsetLimitPagingLoadParams()

        call.respondOffsetLimitPaging(
            params = loadParams,
            pagingSource = schoolDataSource.reportDataSource.listAsPagingSource(
                DataLoadParams(), getListParams
            ).invoke()
        )
    }

    post(ReportDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        val report: List<Report> = call.receive()
        schoolDataSource.reportDataSource.store(report)
        call.respond(HttpStatusCode.NoContent)
    }

}