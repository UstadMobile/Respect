package world.respect.datalayer.respect.model.invite

import kotlinx.serialization.Serializable

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
    val userInviteType: UserInviteType,
) {

    @Suppress("unused")
    enum class UserInviteType {
        TEACHER, STUDENT_OR_PARENT
    }

}

