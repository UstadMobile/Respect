package world.respect.datalayer.http.school

import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.ChangeHistoryEntry
@Serializable
data class DataAndChangeHistory<T>(
    val data: List<T>,
    val changeHistories: List<ChangeHistoryEntry>
)