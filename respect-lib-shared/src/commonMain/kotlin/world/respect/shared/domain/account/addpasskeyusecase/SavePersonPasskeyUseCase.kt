package world.respect.shared.domain.account.addpasskeyusecase

import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.datalayer.AuthenticatedUserPrincipalId

interface SavePersonPasskeyUseCase {

    data class Request(
        val authenticatedUserId: AuthenticatedUserPrincipalId,
        val userGuid: String,
        val passkeyWebAuthNResponse: AuthenticationResponseJSON,
    )

    suspend operator fun invoke(
        request: Request,
    )

}