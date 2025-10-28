package world.respect.datalayer.ext

object PermissionFlags {

    /**
     * COURSE_ permissions will apply to the given course when used as part of a CoursePermission
     * entity, or to all courses if used as part of a SystemPermission entity.
     */

    const val COURSE_LEARNINGRECORD_VIEW = 128L //2^7
}