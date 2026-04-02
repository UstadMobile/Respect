package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.school.model.ChangeHistoryFieldEnum

@Entity
data class ChangeHistoryChangeEntity(

    @PrimaryKey(autoGenerate = true)
    val hcId: Long = 0,

    val hcHistoryGuidHash: Long,

    val hcField: ChangeHistoryFieldEnum,

    val hcOldVal: String?,

    val hcNewVal: String
){
    companion object{
       const val CHILD_COLUMN = "hcHistoryGuidHash"
    }
}