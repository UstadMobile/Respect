package world.respect.datalayer.respect.model.invite

import kotlinx.serialization.Serializable
import world.respect.datalayer.oneroster.rostering.model.OneRosterClassGUIDRef
import world.respect.datalayer.respect.model.RespectRealm

/**
 * @property code the invite code (as provided by the user)
 * @property realm The realm that the user is invited to
 * @property classGUIDRef The class to which the user is being invited
 * @property className The name of the class to which the user is being invited
 * @property schoolName The name of the school to which the user is being invited
 * @property userInviteType type of invite as per the enum
 */
@Serializable
class RespectInviteInfo(
    val code: String,
    val realm: RespectRealm,
    val classGUIDRef: OneRosterClassGUIDRef?,
    val className: String?,
    val schoolName: String?,
    val userInviteType: UserInviteType,
) {

    @Suppress("unused")
    enum class UserInviteType {
        TEACHER, STUDENT_OR_PARENT
    }

}

