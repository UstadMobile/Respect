package world.respect.datalayer.school.ext

import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.school.model.PersonRoleEnum

/**
 * The permission flag required to change a person with this role
 */
val PersonRoleEnum.writePermissionFlag: Long
    get() = when(this) {
        PersonRoleEnum.STUDENT -> PermissionFlags.PERSON_STUDENT_WRITE
        PersonRoleEnum.TEACHER -> PermissionFlags.PERSON_TEACHER_WRITE
        PersonRoleEnum.PARENT -> PermissionFlags.PERSON_PARENT_WRITE
        else -> PermissionFlags.SYSTEM_ADMIN
    }
