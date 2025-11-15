package world.respect.server.routes.school.respect

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.deleteaccount.DeleteAccountUseCase

fun Route.PersonDeleteRoute(
    deleteAccountUseCase: (ApplicationCall) -> DeleteAccountUseCase,
) {
    post("delete") {
        
        val guid = call.request.queryParameters["guid"]
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing guid")

        // pass both redeemRequest and guid to the use case
        call.respond(deleteAccountUseCase(call).invoke (guid))
        
    }
}