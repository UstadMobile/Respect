package world.respect.datalayer.respect.model.invite

import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.PersonRoleEnum

/**
 * @property code the invite code (as provided by the user). An invite code includes a
 *           directory code, then a realm code, and then a code handled by the realm.
 * @property classGuid The guid of the class to which the user is being invited
 * @property className The name of the class to which the user is being invited
 * @property userInviteType type of invite as per the enum
 */
@Serializable
class RespectInviteInfo(
    val code: String,
    val classGuid: String?,
    val className: String?,
    val userInviteType: UserInviteType?=null,
    val role : PersonRoleEnum? = null,
    val familyPersonGuid: String? = null,
    val classRole: EnrollmentRoleEnum? = null,
    val inviteMultipleAllowed: Boolean = false,
    val approvalRequired: Boolean = false,
) {

    @Suppress("unused")
    enum class UserInviteType {
        TEACHER, STUDENT_OR_PARENT
    }
    companion object {
        val INVITE_TYPE_CLASS_CODE = 1
        val INVITE_TYPE_GENERIC = 2
    }

}