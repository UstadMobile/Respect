package world.respect.server.routes.school.respect

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.InviteDataSource
import world.respect.datalayer.school.model.Invite
import world.respect.server.util.ext.requireAccountScope

@Suppress("FunctionName")
fun Route.InviteRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {


    post(InviteDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        val invites: List<Invite> = call.receive()
        schoolDataSource.inviteDataSource.store(invites)
        call.respond(HttpStatusCode.NoContent)
    }
}
