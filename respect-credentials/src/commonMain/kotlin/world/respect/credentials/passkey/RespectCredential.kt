package world.respect.credentials.passkey

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

object RespectCredentialSerializer: JsonContentPolymorphicSerializer<RespectCredential>(
    RespectCredential::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RespectCredential> {
        return if(element.jsonObject.containsKey("username")) {
            RespectPasswordCredential.serializer()
        }else {
            RespectPasskeyCredential.serializer()
        }
    }
}





