package world.respect.server.account.invite

import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.child.GetClassUseCase

fun Route.GetClassUseCaseRoute(
    getClassUseCase: (ApplicationCall) -> GetClassUseCase
) {
    post("name") {
        val classUid = call.request.queryParameters["classUid"]
            ?: throw IllegalStateException("No class found").withHttpStatus(400)

        val response = getClassUseCase(call).invoke(
            classUid = classUid
        )
        call.respond(response)

    }
}