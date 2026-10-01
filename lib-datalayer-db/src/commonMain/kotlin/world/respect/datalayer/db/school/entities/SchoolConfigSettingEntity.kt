package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.school.model.StatusEnum
import kotlin.time.Instant

@Entity
data class SchoolConfigSettingEntity(
    @PrimaryKey
    val scsKey: String,
    val scsValue: String,
    val scsStatus: StatusEnum,
    val scsLastModified: Instant,
    val scsStored: Instant,
    val scsCanReadFlags: Int,
    val scsAnonCanRead: Boolean,
    val scsCanWriteFlags: Int,
)