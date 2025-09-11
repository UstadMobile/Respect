package world.respect.shared.domain.account.setpassword

import world.respect.datalayer.AuthenticatedUserPrincipalId

interface SetPasswordUseCase {

    data class SetPasswordRequest(
        val authenticatedUserId: AuthenticatedUserPrincipalId,
        val userGuid: String,
        val password: String,
    )

    suspend operator fun invoke(
        request: SetPasswordRequest,
    )

}