package world.respect.datalayer.school.domain

import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.school.model.PersonRoleEnum

/**
 * The CheckPersonUseCase is bound (scoped) to a specified authenticated person (the same as the
 * SchoolDataSource itself).
 */
interface CheckPersonPermissionUseCase {

    data class PermissionsRequiredByRole(
        val roleAdminPermissionRequired: Long = PermissionFlags.SYSTEM_ADMIN,
        val roleTeacherPermissionRequired: Long = PermissionFlags.PERSON_TEACHER_WRITE,
        val roleStudentPermissionRequired: Long = PermissionFlags.PERSON_STUDENT_WRITE,
        val roleParentPermissionRequired: Long = PermissionFlags.PERSON_PARENT_WRITE,
    ) {

        companion object {

            val WRITE_PERMISSIONS = PermissionsRequiredByRole(
                roleAdminPermissionRequired = PermissionFlags.SYSTEM_ADMIN,
                roleTeacherPermissionRequired = PermissionFlags.PERSON_TEACHER_WRITE,
                roleStudentPermissionRequired = PermissionFlags.PERSON_STUDENT_WRITE,
                roleParentPermissionRequired =  PermissionFlags.PERSON_PARENT_WRITE,
            )

        }
    }

    /**
     * Check to see if the authenticated user has a permission on another person
     *
     */
    suspend operator fun invoke(
        otherPersonUid: String,
        otherPersonKnownRole: PersonRoleEnum?,
        permissionsRequiredByRole: PermissionsRequiredByRole,
    ): Boolean

}