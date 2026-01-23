package world.respect.datalayer.school.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.Invite2.Companion.TYPE_CLASS
import world.respect.datalayer.school.model.Invite2.Companion.TYPE_FAMILY_MEMBER
import world.respect.datalayer.school.model.Invite2.Companion.TYPE_NEW_USER
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

/**
 * An invitation for RESPECT can be one of three types.
 *
 * Serialization is as per:
 * https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#custom-subclass-serial-name
 *
 * @property uid the uid will be in the form of invite type:details e.g. newuser:role, class:role/classuid etc.
 *           this ensures there is one invite per type.
 * @property code the invite code
 * @property approvalRequiredAfter Users with permission to invite others may turn off the
 *           requirement to approve a user for a short interval (e.g. 15 minutes). This allows
 *           skipping the admin approval requirement (e.g. during teacher training workshops)
 *           whilst mitigating the risk that the link might be shared with unauthorized people later.
 */
@Serializable
sealed interface Invite2: ModelWithTimes {
    val uid: String
    val code: String
    val approvalRequiredAfter: InstantAsISO8601
    override val lastModified: InstantAsISO8601
    override val stored: InstantAsISO8601

    val status: StatusEnum


    companion object {

        const val TYPE_NEW_USER = "newuser"

        const val TYPE_CLASS = "class"

        const val TYPE_FAMILY_MEMBER = "familymember"

    }
}

@Serializable
@SerialName(TYPE_NEW_USER)
data class NewUserInvite(
    override val uid: String,
    override val code: String,
    override val approvalRequiredAfter: InstantAsISO8601,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    override val status: StatusEnum = StatusEnum.ACTIVE,
    val role: PersonRoleEnum,
    val firstUser: Boolean = false,
): Invite2

@Serializable
@SerialName(TYPE_CLASS)
data class ClassInvite(
    override val uid: String,
    override val code: String,
    override val approvalRequiredAfter: InstantAsISO8601,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    override val status: StatusEnum = StatusEnum.ACTIVE,
    val classUid: String,
    val role: EnrollmentRoleEnum,
): Invite2

@Serializable
@SerialName(TYPE_FAMILY_MEMBER)
data class FamilyMemberInvite(
    override val uid: String,
    override val code: String,
    override val approvalRequiredAfter: InstantAsISO8601,
    override val lastModified: InstantAsISO8601 = Clock.System.now(),
    override val stored: InstantAsISO8601 = Clock.System.now(),
    override val status: StatusEnum = StatusEnum.ACTIVE,
    val personUid: String,
): Invite2


/**
 * @property guid: always in the form of type:id
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

        const val EXPIRATION_TIME: Long = (7 * 24 * 60 * 60 * 1000)

    }
}