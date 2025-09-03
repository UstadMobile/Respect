package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * @param submitterUid the personUid for individual assignments, group number for group assignments.
 * 0 if the active person is not a submitter
 * @param hasModeratePermission Boolean indicating if the active user can moderate comments
 */
@Serializable
data class ClazzAssignmentCourseBlockAndSubmitterUid(
    @Embedded
    var clazzAssignment: ClazzAssignment? = null,
    @Embedded
    var courseBlock: CourseBlock? = null,

    @Embedded
    var courseBlockPicture: CourseBlockPicture? = null,

    @Embedded
    var courseGroupSet: CourseGroupSet? = null,

    var submitterUid: Long = 0,

    var hasModeratePermission: Boolean = false,
)
