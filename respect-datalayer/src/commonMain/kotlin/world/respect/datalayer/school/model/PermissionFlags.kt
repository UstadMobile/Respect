package world.respect.datalayer.school.model

@Suppress("unused")
object PermissionFlags {

    const val CLASS_READ = 1

    const val CLASS_WRITE = 2

    const val PERSON_STUDENT_READ = 4

    const val PERSON_STUDENT_WRITE = 8

    const val PERSON_TEACHER_READ = 16

    /**
     * Permission to modify teachers, or add new users with the teacher role.
     */
    const val PERSON_TEACHER_WRITE = 32

}