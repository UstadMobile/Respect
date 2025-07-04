package world.respect.app.domain.account

import kotlinx.serialization.Serializable

/**
 * Placeholder representing a user account (likely linked to an upstream xAPI and OneRoster server)
 */
@Serializable
data class RespectAccount(
    val accountId: String,
)
