package world.respect.datalayer.respect.model

import io.ktor.http.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import world.respect.datalayer.respect.model.invite.AuthOptionConfigTypeEnum


@Serializable
sealed interface AuthOptionConfig {

    val type: AuthOptionConfigTypeEnum

}

@Serializable
@SerialName("builtin")
data class BuiltinAuthOptionConfig(
    override val type: AuthOptionConfigTypeEnum = AuthOptionConfigTypeEnum.BUILTIN
) : AuthOptionConfig
@Serializable
@SerialName("openid")
data class OpenIdAuthOptionConfig(
    override val type: AuthOptionConfigTypeEnum = AuthOptionConfigTypeEnum.OPENID,
    val issuer: Url,
) : AuthOptionConfig

