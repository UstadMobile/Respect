package world.respect.shared.domain.account.gettokenanduser

import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.shared.domain.account.AuthResponse

interface GetTokenAndUserProfileWithPasskeyUseCase {

    suspend operator fun invoke(
        authJson: AuthenticationResponseJSON
    ): AuthResponse

}