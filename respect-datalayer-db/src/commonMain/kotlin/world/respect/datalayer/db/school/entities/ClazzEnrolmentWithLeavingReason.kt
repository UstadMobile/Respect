package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzEnrolmentWithLeavingReason : ClazzEnrolment() {

    @Embedded
    var leavingReason: LeavingReason? = null

    var timeZone : String? = null

}