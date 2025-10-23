package world.respect.datalayer.school.model

@Suppress("unused")
object PermissionFlags {

    const val CLASS_READ = 1L

    const val CLASS_WRITE = 2L

    const val PERSON_STUDENT_READ = 4L

    const val PERSON_STUDENT_WRITE = 8L

    const val PERSON_PARENT_READ = 16L

    const val PERSON_PARENT_WRITE = 32L

    const val PERSON_TEACHER_READ = 64L

    /**
     * Permission to modify teachers, or add new users with the teacher role.
     */
    const val PERSON_TEACHER_WRITE = 128L

    /**
     * When used as a SchoolPermission, then that role will have permission to add any student user
     * in the school to any class in the school.
     */
    const val ADD_STUDENT_TO_CLASS = 256L

    const val ADD_TEACHER_TO_CLASS = 512L

    const val INVITE_NEW_USER = 1024L

    const val SYSTEM_ADMIN = Long.MAX_VALUE

    const val TEACHER_DEFAULT_PERMISSIONS = CLASS_READ
            .or(CLASS_WRITE)
            .or(PERSON_STUDENT_READ)
            .or(PERSON_STUDENT_WRITE)
            .or(PERSON_PARENT_READ)
            .or(PERSON_PARENT_WRITE)
            .or(PERSON_TEACHER_READ)
            .or(ADD_STUDENT_TO_CLASS)
            .or(ADD_TEACHER_TO_CLASS)
            .or(INVITE_NEW_USER)

    const val STUDENT_DEFAULT_PERMISSIONS = CLASS_READ

    const val PARENT_DEFAULT_PERMISSIONS = CLASS_READ

    const val SYSADMIN_DEFAULT_PERMISSIONS = SYSTEM_ADMIN


}