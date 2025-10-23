package world.respect.datalayer.db.school.domain

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.SchoolPermissionGrant

class AddDefaultSchoolPermissionGrantsUseCase(
    private val dataSource: SchoolDataSource
) {

    private fun PersonRoleEnum.newInitialGrant(
        permissions: Long
    ) = SchoolPermissionGrant(
        uid = this.flag.toString(),
        toRole = this,
        permissions = permissions,
    )

    suspend operator fun invoke() {
        dataSource.schoolPermissionGrantDataSource.store(
            listOf(
                PersonRoleEnum.SYSTEM_ADMINISTRATOR.newInitialGrant(
                    PermissionFlags.SYSADMIN_DEFAULT_PERMISSIONS
                ),
                PersonRoleEnum.TEACHER.newInitialGrant(
                    PermissionFlags.TEACHER_DEFAULT_PERMISSIONS
                ),
                PersonRoleEnum.STUDENT.newInitialGrant(
                    PermissionFlags.STUDENT_DEFAULT_PERMISSIONS
                ),
                PersonRoleEnum.PARENT.newInitialGrant(
                    PermissionFlags.PARENT_DEFAULT_PERMISSIONS
                ),
            )
        )
    }
}