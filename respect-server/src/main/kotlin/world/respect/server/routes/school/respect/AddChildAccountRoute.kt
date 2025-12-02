package world.respect.server.routes.school.respect

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.child.AddChildAccountUseCase
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest

fun Route.AddChildAccountRoute(
    addChildAccountUseCase: (ApplicationCall) -> AddChildAccountUseCase
) {
    post("addchild") {
        val personInfo: RespectRedeemInviteRequest.PersonInfo = call.receive()
        val parentUsername = call.request.queryParameters["parentUsername"]
            ?: throw IllegalArgumentException("missing parentUsername param").withHttpStatus(400)
        val classUid = call.request.queryParameters["classUid"]
        val inviteCode = call.request.queryParameters["inviteCode"]
            ?: throw IllegalArgumentException("missing inviteCode param").withHttpStatus(400)
        call.respond(
            addChildAccountUseCase(call).invoke(
                personInfo = personInfo,
                parentUsername = parentUsername,
                classUid = classUid,
                inviteCode = inviteCode
            )
        )
    }

}