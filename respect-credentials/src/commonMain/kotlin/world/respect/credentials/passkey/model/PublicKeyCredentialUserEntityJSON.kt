package world.respect.credentials.passkey.model

import kotlinx.serialization.Serializable

@Serializable
data class PublicKeyCredentialUserEntityJSON(
    val id: String,
    val name: String,
    val displayName: String,
)
