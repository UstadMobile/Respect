package world.respect.server.routes.school.respect

import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase

fun Route.InviteInfoRoute(
    getInviteInfoUseCase: (ApplicationCall) -> GetInviteInfoUseCase,
) {
    get("info") {
        val type = call.request.queryParameters["type"]
        val code = call.request.queryParameters["code"]
            ?: throw IllegalArgumentException("missing code param").withHttpStatus(400)

        call.respond(
            getInviteInfoUseCase(call).invoke(
                code,
                type?.toInt() ?: RespectInviteInfo.INVITE_TYPE_CLASS_CODE
            )
        )
    }
}