package world.respect.datalayer.db.school.entities

import kotlinx.serialization.Serializable

@Serializable
data class PeerReviewerAllocationList(val allocations: List<PeerReviewerAllocation>?)