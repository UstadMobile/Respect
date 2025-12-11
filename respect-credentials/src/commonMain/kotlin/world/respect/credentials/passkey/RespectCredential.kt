package world.respect.credentials.passkey

import io.ktor.http.Url
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import world.respect.credentials.passkey.model.AuthenticationResponseJSON

/**
 * Sealed class that represents a password or passkey credential
 */
@Serializable(with = RespectCredentialSerializer::class)
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

@Serializable
data class RespectQRBadgeCredential(
    val qrCodeUrl: Url
): RespectCredential()

object RespectCredentialSerializer: JsonContentPolymorphicSerializer<RespectCredential>(
    RespectCredential::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RespectCredential> {
        val jsonObject = element.jsonObject
        return when {
            jsonObject.containsKey("username") -> RespectPasswordCredential.serializer()
            jsonObject.containsKey("qrCodeUrl") -> RespectQRBadgeCredential.serializer()
            else -> RespectPasskeyCredential.serializer()
        }
    }
}





