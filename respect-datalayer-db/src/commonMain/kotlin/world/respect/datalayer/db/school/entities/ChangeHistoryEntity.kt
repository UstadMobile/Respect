package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.school.model.ChangeHistoryTableEnum

@Entity
data class ChangeHistoryEntity(
    val hGuid: String,
    @PrimaryKey
    val hGuidHash: Long,
    val hTable: ChangeHistoryTableEnum,
    val hTableGuid: String,
    val hTimestamp: Long,
    val hWhoGuid: String,
    val hWhoGuidHash: Long,
){
    companion object{
       const val PARENT_COLUMN = "hGuidHash"
    }
}