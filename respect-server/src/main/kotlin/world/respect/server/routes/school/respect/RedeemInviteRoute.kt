package world.respect.server.routes.school.respect

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.shared.domain.account.invite.RedeemInviteExistingUserUseCase
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.domain.account.invite.RedeemInviteUseCase

fun Route.RedeemInviteRoute(
    redeemInviteUseCase: (ApplicationCall) -> RedeemInviteUseCase,
    redeemInviteExistingUserUseCase: (ApplicationCall) -> RedeemInviteExistingUserUseCase,
) {

    post("redeem") {
        val redeemRequest: RespectRedeemInviteRequest = call.receive()
        call.respond(redeemInviteUseCase(call).invoke(redeemRequest))
    }
    post("existingUserRedeem") {
        val selectedChildGuid = call.request.queryParameters["selectedChildGuid"]

        val redeemRequest: RespectRedeemInviteRequest = call.receive()
        call.respond(redeemInviteExistingUserUseCase(call).invoke(redeemRequest,selectedChildGuid))
    }
}