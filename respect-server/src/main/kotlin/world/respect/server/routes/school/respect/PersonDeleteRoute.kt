package world.respect.server.routes.school.respect

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
            ?: throw IllegalStateException("No person found").withHttpStatus(400)

        val response = deleteAccountUseCase(call).invoke(
            guid = guid
        )
        call.respond(response)

    }}