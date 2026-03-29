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
import world.respect.datalayer.school.BookmarkDataSource
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.server.util.ext.offsetLimitPagingLoadParams
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondDataLoadState
import world.respect.server.util.ext.respondOffsetLimitPaging


@Suppress("FunctionName")
fun Route.BookmarkRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {

    get(BookmarkDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        call.respondDataLoadState(
            schoolDataSource.bookmarkDataSource.list(
                loadParams = DataLoadParams(),
                listParams = BookmarkDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                )
            )
        )
    }

    post(BookmarkDataSource.ENDPOINT_NAME) {
        schoolDataSource(call).bookmarkDataSource.store(
            list = call.receive()
        )
        call.respond(HttpStatusCode.NoContent)
    }
}