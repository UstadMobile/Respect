package world.respect.credentials.passkey

import kotlinx.serialization.Serializable
import world.respect.credentials.passkey.model.AuthenticationResponseJSON

/**
 * Sealed class that represents a password or passkey credential
 */
@Serializable
sealed class RespectCredential

@Serializable
data class RespectPasswordCredential(
    val username: String,
    val password: String,
): RespectCredential()

@Serializable
data class RespectPasskeyCredential(
    val passkeyWebAuthNResponse: AuthenticationResponseJSON
): RespectCredential()






