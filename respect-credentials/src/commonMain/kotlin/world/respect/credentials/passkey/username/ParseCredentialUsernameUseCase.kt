package world.respect.credentials.passkey.username

import io.ktor.http.Url
import world.respect.credentials.passkey.util.requirePostfix

/**
 * When using a saved credential, the username will include the url as outlined
 * in CreateCredentialUsernameUseCase
 */
class ParseCredentialUsernameUseCase {

    /**
     * @param credentialUsername the credential username as per CreateCredentialUsernameUseCase doc
     * @return a pair containing the url and username itself
     */
    operator fun invoke(credentialUsername: String): Pair<Url, String> {
        val (username, learningSpacePart) = credentialUsername.split("@", limit = 2)
        val learningSpacePartLower = learningSpacePart.lowercase()

        return if(learningSpacePartLower.startsWith("http://")) {
            Pair(Url(learningSpacePart), username)
        }else {
            Pair(Url("https://$learningSpacePart".requirePostfix("/")), username)
        }
    }

}