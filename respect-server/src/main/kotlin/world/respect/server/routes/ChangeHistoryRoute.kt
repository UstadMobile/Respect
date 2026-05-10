package world.respect.server.routes


import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.ChangeHistoryDataSource
import world.respect.server.util.ext.offsetLimitPagingLoadParams
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondOffsetLimitPaging

@Suppress("FunctionName")
fun Route.ChangeHistoryRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(ChangeHistoryDataSource.ENDPOINT_NAME) {

        val schoolDataSource = schoolDataSource(call)

        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val getListParams = ChangeHistoryDataSource.GetListParams.fromParams(
            call.request.queryParameters
        )

        val loadParams = call.request.queryParameters.offsetLimitPagingLoadParams()

        call.respondOffsetLimitPaging(
            params = loadParams,
            pagingSource = schoolDataSource.changeHistoryDataSource
                .listAsPagingSource(
                    DataLoadParams(),
                    getListParams
                ).invoke()
        )
    }
}

