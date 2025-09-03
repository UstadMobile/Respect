package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class PeerReviewerAllocation(
    @PrimaryKey(autoGenerate = true)
    var praUid: Long = 0,

    // peer that is marking the assignment
    var praMarkerSubmitterUid: Long = 0,

    // peer that is being marked
    var praToMarkerSubmitterUid: Long = 0,

    var praAssignmentUid: Long = 0,

    var praActive: Boolean = true,

    var praLct: Long = 0,
) {

    companion object {

        const val TABLE_ID = 140

    }


}