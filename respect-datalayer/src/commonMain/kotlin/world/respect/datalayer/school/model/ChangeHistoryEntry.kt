package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable

@Serializable
data class ChangeHistoryEntry(
    val guid: String,
    val table: ChangeHistoryTableEnum,
    val timestamp: Long,
    val whoGuid: String,
    val changes: List<ChangeHistoryChange>
)

@Serializable
data class ChangeHistoryChange(
    val field: ChangeHistoryFieldEnum,
    val newVal: String,
    val oldVal: String?
)