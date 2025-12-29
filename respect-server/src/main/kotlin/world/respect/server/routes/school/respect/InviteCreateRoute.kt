package world.respect.server.routes.school.respect

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import world.respect.datalayer.school.model.Invite
import world.respect.shared.domain.account.invite.CreateInviteUseCase

fun Route.InviteCreateRoute(
    createInviteUseCase: (ApplicationCall) -> CreateInviteUseCase,
) {
    post("create") {
        val invite = call.receive<Invite>()

        val createdInvite = createInviteUseCase(call)(invite)

        call.respond(createdInvite)
    }
}
