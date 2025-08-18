package world.respect.shared.domain.account.gettokenanduser

import world.respect.shared.domain.account.AuthResponse

/**
 * Gets a token and user profile given a username and password.
 *
 * Server implementation: creates a token entity in the database (if valid) and then returns the
 * related user profile and token (e.g. for Authorization: Bearer ...).
 *
 * Client implementation: sends username and password to server and receives the token and user
 * profile.
 */
interface GetTokenAndUserProfileWithUsernameAndPasswordUseCase {

    suspend operator fun invoke(
        username: String,
        password: String
    ): AuthResponse

    companion object {

        const val PARAM_NAME_USERNAME = "username"

    }

}