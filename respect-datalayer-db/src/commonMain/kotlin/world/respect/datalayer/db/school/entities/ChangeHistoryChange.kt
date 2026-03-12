package world.respect.datalayer.db.school.entities

import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.ChangeHistoryFieldEnum

@Serializable
data class ChangeHistoryChange(
    val field: ChangeHistoryFieldEnum,
    val newVal: String,
    val oldVal: String?
)