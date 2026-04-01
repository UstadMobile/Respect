package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.school.model.ChangeHistoryFieldEnum
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
data class ChangeHistoryChangeEntity(

    @PrimaryKey(autoGenerate = true)
    val hcId: Long = 0,

    val hcHistoryGuidHash: Long,

    val hcField: ChangeHistoryFieldEnum,

    val hcOldVal: String?,

    val hcNewVal: String,

    val hcSynced: Boolean = false,
    val hcLastModified: Instant = Clock.System.now(),
    val hcStored: Instant = Clock.System.now(),
){
    companion object{
       const val CHILD_COLUMN = "hcHistoryGuidHash"
    }
}