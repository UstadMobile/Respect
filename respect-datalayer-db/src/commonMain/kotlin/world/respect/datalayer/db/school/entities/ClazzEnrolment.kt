package world.respect.datalayer.db.school.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

import kotlinx.serialization.Serializable
import world.respect.datalayer.db.shared.ext.UNSET_DISTANT_FUTURE

/**
 * This entity represents a person's enrolment in a course. This can be a teacher or student. One
 * person may have more than one enrolment in a course (e.g. if they dropout and then return). They
 * should not have overlapping enrolments (e.g. more than one enrolment active at the same time).
 *
 * When a student requests to join a course (e.g. using the course code) they have an enrolment
 * with the role pending student. This can then be converted into a student role when approved.
 */
@Entity(indices = [
    //Index to streamline permission queries etc. that lookup a list of classes for a given person
    Index(value = ["clazzEnrolmentPersonUid", "clazzEnrolmentClazzUid"]),
    //Index to streamline finding which people are in a given clazzuid
    Index(value = ["clazzEnrolmentClazzUid", "clazzEnrolmentPersonUid"]),
    //Index for streamlining ClazzList where the number of users is counted by role
    Index(value = ["clazzEnrolmentClazzUid", "clazzEnrolmentRole"])
])
@Serializable
open class ClazzEnrolment()  {

    @PrimaryKey(autoGenerate = true)
    var clazzEnrolmentUid: Long = 0

    /**
     * The personUid of the person enroled into the course
     */
    @ColumnInfo(index = true)
    var clazzEnrolmentPersonUid: Long = 0

    /**
     * The clazzUid of the course
     */
    @ColumnInfo(index = true)
    var clazzEnrolmentClazzUid: Long = 0

    var clazzEnrolmentDateJoined: Long = 0

    /**
     * The date the student left this class (e.g. graduated or un-enrolled).
     * Long.MAX_VALUE = no leaving date (e.g. ongoing registration)
     */
    var clazzEnrolmentDateLeft: Long = UNSET_DISTANT_FUTURE

    var clazzEnrolmentRole: Int = 0

    var clazzEnrolmentAttendancePercentage: Float = 0.toFloat()

    var clazzEnrolmentActive: Boolean = true

    var clazzEnrolmentLeavingReasonUid: Long = 0

    var clazzEnrolmentOutcome: Int = OUTCOME_IN_PROGRESS


    var clazzEnrolmentLocalChangeSeqNum: Long = 0


    var clazzEnrolmentMasterChangeSeqNum: Long = 0


    var clazzEnrolmentLastChangedBy: Int = 0

    var clazzEnrolmentLct: Long = 0

    var clazzEnrolmentInviteUid: Long = 0

    constructor(clazzUid: Long, personUid: Long) : this() {
        this.clazzEnrolmentClazzUid = clazzUid
        this.clazzEnrolmentPersonUid = personUid
        this.clazzEnrolmentActive = true
    }

    constructor(clazzUid: Long, personUid: Long, role: Int):this() {
        this.clazzEnrolmentClazzUid = clazzUid
        this.clazzEnrolmentPersonUid = personUid
        this.clazzEnrolmentRole = role
        this.clazzEnrolmentActive = true
    }

    companion object {

        const val ROLE_STUDENT = 1000

        const val ROLE_TEACHER = 1001

        /**
         * The role given to someone who has the class code, however their enrolment is not yet approved.
         */
        const val ROLE_STUDENT_PENDING = 1002

        const val ROLE_PARENT = 1003

        const val OUTCOME_IN_PROGRESS = 200

        const val OUTCOME_GRADUATED = 201

        const val OUTCOME_FAILED = 202

        const val OUTCOME_DROPPED_OUT = 203

        const val TABLE_ID = 65
    }
}
