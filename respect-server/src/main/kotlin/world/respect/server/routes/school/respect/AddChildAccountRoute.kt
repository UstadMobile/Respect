package world.respect.server.routes.school.respect

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.shared.domain.account.child.AddChildAccountUseCase
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest

fun Route.AddChildAccountRoute(
    addChildAccountUseCase: (ApplicationCall) -> AddChildAccountUseCase
) {
    post("addchild") {
        val personInfo: RespectRedeemInviteRequest.PersonInfo = call.receive()
        call.respond(addChildAccountUseCase(call).invoke(personInfo))
    }

}