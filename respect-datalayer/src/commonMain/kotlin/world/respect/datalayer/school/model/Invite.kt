package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

/**
 * @property code invite code (shared with users to join a class/family etc.)
 * @property newRole role to be assigned to the invited user
 * @property forFamilyOfGuid optional — if this invite is for a specific family group
 * @property forClassGuid optional — if this invite is for a specific class
 * @property forClassRole role within the class that the invite corresponds to
 * @property inviteMultipleAllowed whether multiple users can use the same invite code
 * @property approvalRequired whether manual approval is required for invite acceptance
 * @property expiration timestamp (epoch millis) when the invite expires
 */
@Serializable
data class Invite(
    val guid: String,
    val code: String,
    val newRole: PersonRoleEnum? = null,
    val forFamilyOfGuid: String? = null,
    val forClassGuid: String? = null,
    val forClassName: String? = null,
    val schoolName: String? = null,
    val forClassRole: EnrollmentRoleEnum? = null,
    val inviteMultipleAllowed: Boolean = false,
    val approvalRequired: Boolean = false,
    val firstUser: Boolean = false,
    val expiration: InstantAsISO8601,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    val inviteStatus :InviteStatusEnum = InviteStatusEnum.PENDING
) : ModelWithTimes {

    companion object {
        const val TABLE_ID = 17

        const val EXPIRATION_TIME:Long = (7 * 24 * 60 * 60 * 1000)

    }
}