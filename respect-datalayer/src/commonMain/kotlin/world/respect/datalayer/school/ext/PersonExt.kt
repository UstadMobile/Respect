package world.respect.datalayer.school.ext

import world.respect.datalayer.exceptions.ForbiddenException
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum

fun Person?.assertPersonHasRole(
    role: PersonRoleEnum
) {
    if(this?.roles?.any { it.roleEnum == role } != true) {
        throw ForbiddenException("Person does not have ${role.value} role")
    }
}
