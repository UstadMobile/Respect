package world.respect.server.routes.school.respect

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.shared.domain.account.invite.RedeemInviteExistingUserUseCase
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest

fun Route.RedeemInviteExistingUserRoute(
    redeemInviteExistingUserUseCase  : (ApplicationCall) -> RedeemInviteExistingUserUseCase
) {

    post("existingUserRedeem") {
        val selectedChildGuid = call.request.queryParameters["selectedChildGuid"]
        try {
            val redeemRequest: RespectRedeemInviteRequest = call.receive()

            redeemInviteExistingUserUseCase(call).invoke(redeemRequest, selectedChildGuid)
            call.respond(HttpStatusCode.OK)
        } catch (e: Throwable) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown error")
        }
    }
}