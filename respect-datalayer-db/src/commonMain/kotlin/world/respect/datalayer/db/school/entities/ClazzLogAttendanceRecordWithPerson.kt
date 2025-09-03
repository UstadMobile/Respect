package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.Person

@Serializable
class ClazzLogAttendanceRecordWithPerson : ClazzLogAttendanceRecord() {

    @Embedded
    var person: Person? = null

    fun copy() = ClazzLogAttendanceRecordWithPerson().also {
        it.person = person
        it.clazzLogAttendanceRecordUid = clazzLogAttendanceRecordUid
        it.clazzLogAttendanceRecordPersonUid = clazzLogAttendanceRecordPersonUid
        it.clazzLogAttendanceRecordClazzLogUid = clazzLogAttendanceRecordClazzLogUid
        it.clazzLogAttendanceRecordLastChangedBy = clazzLogAttendanceRecordLastChangedBy
        it.clazzLogAttendanceRecordLocalChangeSeqNum = clazzLogAttendanceRecordLocalChangeSeqNum
        it.clazzLogAttendanceRecordMasterChangeSeqNum = clazzLogAttendanceRecordMasterChangeSeqNum
        it.attendanceStatus = attendanceStatus
    }

}
