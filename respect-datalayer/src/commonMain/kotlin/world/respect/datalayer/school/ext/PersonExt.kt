package world.respect.datalayer.school.ext

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import world.respect.datalayer.exceptions.ForbiddenException
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum

fun Person?.assertPersonHasRole(
    role: PersonRoleEnum
) {
    if(this?.roles?.any { it.roleEnum == role } != true) {
        throw ForbiddenException("Person does not have ${role.value} role")
    }
}

fun Person.primaryRole(): PersonRoleEnum {
    return roles.first { it.isPrimaryRole }.roleEnum
}

/**
 * Put the invite code in the metadata of the Person
 */
fun Person.copyWithInviteInfo(
    invite: Invite2
): Person {
    return copy(
        metadata = buildJsonObject {
            this@copyWithInviteInfo.metadata?.also {
                putAll(it)
            }

            put(Person.METADATA_KEY_INVITE_ID, JsonPrimitive(invite.code))
            put(Person.METADATA_KEY_INVITE_UID, JsonPrimitive(invite.uid))
        }
    )
}

fun Person.inviteCodeOrNull(): String? {
    return metadata?.get(Person.METADATA_KEY_INVITE_ID)?.jsonPrimitive?.contentOrNull
}

fun Person.inviteUidOrNull(): String? {
    return metadata?.get(Person.METADATA_KEY_INVITE_UID)?.jsonPrimitive?.contentOrNull
}
