package world.respect.server.routes

import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.server.util.ext.getSchoolKoinScope
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithUsernameAndPasswordUseCase
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithUsernameAndPasswordUseCase.Companion.PARAM_NAME_USERNAME

/**
 * Routes that handle issuing tokens.
 */
fun Route.AuthRoute() {

    //When credentials module is m
    post("auth-with-password") {
        val username = call.request.queryParameters[PARAM_NAME_USERNAME]
            ?: throw IllegalArgumentException()
        val password = call.receiveText().trim()
        val schoolScope = call.getSchoolKoinScope()

        val getTokenUseCase: GetTokenAndUserProfileWithUsernameAndPasswordUseCase = schoolScope.get()
        val authResponse = getTokenUseCase(username, password)

        call.respond(authResponse)
    }

}