package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import world.respect.datalayer.db.shared.ext.PermissionFlags.COURSE_EDIT
import world.respect.datalayer.db.shared.ext.PermissionFlags.COURSE_VIEW
import world.respect.datalayer.db.shared.ext.PermissionFlags.PERSON_VIEW
import world.respect.datalayer.db.shared.ext.PermissionFlags.COURSE_MODERATE
import world.respect.datalayer.db.shared.ext.PermissionFlags.COURSE_ATTENDANCE_VIEW
import world.respect.datalayer.db.shared.ext.PermissionFlags.COURSE_ATTENDANCE_RECORD
import world.respect.datalayer.db.shared.ext.PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT
import world.respect.datalayer.db.shared.ext.PermissionFlags.COURSE_MANAGE_TEACHER_ENROLMENT
import world.respect.datalayer.db.shared.ext.PermissionFlags.COURSE_LEARNINGRECORD_VIEW
import world.respect.datalayer.db.shared.ext.PermissionFlags.COURSE_LEARNINGRECORD_EDIT


/**
 * @param cpToEnrolmentRole permissions will be given to anyone who is a member of the course with
 *        the specified role.
 * @param cpToPersonUid permissions will be given to a specific personUid
 * @param cpToGroupUid permissions will be given to a specific group (placeholder, reserved for future use)
 * @param cpPermissionsFlag permissions to be granted as per PermissionFlags constants
 *
 */
@Entity(
    indices = arrayOf(
        Index("cpClazzUid", name = "idx_coursepermission_clazzuid")
    )
)
@Serializable
data class CoursePermission(
    @PrimaryKey(autoGenerate = true)
    var cpUid: Long = 0,

    var cpLastModified: Long = 0,

    var cpClazzUid: Long = 0,

    var cpToEnrolmentRole: Int= 0,

    var cpToPersonUid: Long = 0,

    var cpToGroupUid: Long = 0,

    var cpPermissionsFlag: Long = 0,

    var cpIsDeleted: Boolean = false,
) {

    companion object {

        const val TABLE_ID = 10012


        const val TEACHER_DEFAULT_PERMISSIONS = COURSE_VIEW or
                COURSE_EDIT or
                PERSON_VIEW or
                COURSE_MODERATE or
                COURSE_MANAGE_STUDENT_ENROLMENT or
                COURSE_MANAGE_TEACHER_ENROLMENT or
                COURSE_ATTENDANCE_VIEW or
                COURSE_ATTENDANCE_RECORD or
                COURSE_LEARNINGRECORD_VIEW or
                COURSE_LEARNINGRECORD_EDIT


        const val STUDENT_DEFAULT_PERMISSIONS = COURSE_VIEW or
                PERSON_VIEW


    }
}
