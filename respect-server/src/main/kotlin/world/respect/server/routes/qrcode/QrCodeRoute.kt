package world.respect.server.routes.qrcode

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.PersonQrDataSource
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondDataLoadState

fun Route.QrCodeRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(PersonQrDataSource.ENDPOINT_NAME) {
        call.respondDataLoadState(
            schoolDataSource(call).personQrDataSource.listAll(
                loadParams = DataLoadParams(),
                listParams = PersonQrDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                )
            )
        )
    }

    post(PersonQrDataSource.ENDPOINT_NAME) {
        schoolDataSource(call).personQrDataSource.store(call.receive())
        call.respond(HttpStatusCode.NoContent)
    }
}