package world.respect.credentials.passkey.model // Or your preferred package

import kotlinx.serialization.Serializable
import java.util.UUID
/**
reference on AAGUID]
https://developer.mozilla.org/en-US/docs/Web/API/Web_Authentication_API/Authenticator_data#attestedcredentialdata
 */
@Serializable
data class AaguidProviderInfo(
    val name: String?
)
//https://github.com/passkeydeveloper/passkey-authenticator-aaguids/blob/main/aaguid.json
typealias AaguidProviderData = Map<String, AaguidProviderInfo>

data class PasskeyProviderInfo(
    val aaguid: UUID,
    val name: String
)
