package world.respect.server.routes.passkey

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.shared.domain.account.addpasskeyusecase.SavePersonPasskeyUseCase

fun Route.SavePersonPasskeyRoute(
    useCase: (ApplicationCall) -> SavePersonPasskeyUseCase,
) {
    post("savepersonpasskey") {

        val request: SavePersonPasskeyUseCase.Request = call.receive()
        val response = useCase(call).invoke(
            request = request
        )
        call.respond(response)
    }
}

