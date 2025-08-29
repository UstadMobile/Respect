package world.respect.datalayer.db.realm.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReportEntity (
    @PrimaryKey
    val reportId: String,
    val ownerGuid: String,
    val title: String,
    val reportOptions: String,
    val reportIsTemplate: Boolean = false,
    val active: Boolean = true,
)