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
import world.respect.datalayer.school.IndicatorDataSource
import world.respect.datalayer.school.model.Indicator
import world.respect.server.util.ext.offsetLimitPagingLoadParams
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondOffsetLimitPaging

@Suppress("FunctionName")
fun Route.IndicatorRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(IndicatorDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        val getListParams = IndicatorDataSource.GetListParams.fromParams(call.request.queryParameters)

        val loadParams = call.request.queryParameters.offsetLimitPagingLoadParams()

        call.respondOffsetLimitPaging(
            params = loadParams,
            pagingSource = schoolDataSource.indicatorDataSource.listAsPagingSource(
                DataLoadParams(), getListParams
            ).invoke()
        )
    }

    post(IndicatorDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        val indicator: List<Indicator> = call.receive()
        schoolDataSource.indicatorDataSource.store(indicator)
        call.respond(HttpStatusCode.NoContent)
    }
}