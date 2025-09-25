package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class IndicatorEntity(
    val iGuid: String,
    @PrimaryKey
    val iGuidHash: Long = 0,
    val iName: String = "",
    val iDescription: String = "",
    val iType: String = "",
    val iSql: String = "",
    val iStored:Long,
    val iLastModified: Long
)