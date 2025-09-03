package world.respect.datalayer.db.school.entities

import kotlinx.serialization.Serializable

@Serializable
data class AssignmentSubmitterSummary(
    var submitterUid: Long = 0,

    var name: String? = null,

    var pictureUri: String? = null,

    var latestPrivateComment: String? = null,

    var fileSubmissionStatus: Int = 0,
)
