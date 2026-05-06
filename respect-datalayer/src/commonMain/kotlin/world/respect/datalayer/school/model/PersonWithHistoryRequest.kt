package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable

@Serializable
data class PersonWithHistoryRequest(
    val person: List<Person>,
    val changeHistory: List<ChangeHistoryEntry>
)