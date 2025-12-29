package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.lib.serializers.InstantAsISO8601

@Serializable
data class PullSyncStatus(
    val accountPersonUid: String,
    val consistentThrough: InstantAsISO8601,
    val tableId: Int,
)
