package world.respect.shared.domain.account.addpasskeyusecase

import kotlinx.serialization.Serializable
import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.datalayer.AuthenticatedUserPrincipalId

interface SavePersonPasskeyUseCase {
    @Serializable
    data class Request(
        val authenticatedUserId: AuthenticatedUserPrincipalId,
        val userGuid: String,
        val passkeyWebAuthNResponse: AuthenticationResponseJSON,
        val deviceName: String,
    )

    suspend operator fun invoke(
        request: Request,
    )

}