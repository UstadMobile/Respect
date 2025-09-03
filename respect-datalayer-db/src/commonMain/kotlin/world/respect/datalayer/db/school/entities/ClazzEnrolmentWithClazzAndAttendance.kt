package world.respect.datalayer.db.school.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzEnrolmentWithClazzAndAttendance : ClazzEnrolmentWithClazz() {

    var attendance: Float = 0f

}