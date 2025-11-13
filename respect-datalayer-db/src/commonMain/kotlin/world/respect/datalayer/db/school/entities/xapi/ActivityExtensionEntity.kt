package world.respect.datalayer.db.school.entities.xapi

import androidx.room.Entity

@Entity(primaryKeys = ["aeeActivityUid", "aeeKeyHash"])
data class ActivityExtensionEntity(
    var aeeActivityUid: Long = 0,

    var aeeKeyHash: Long = 0,

    var aeeKey: String? = null,

    var aeeJson: String? = null,

    var aeeLastMod: Long = 0,

    var aeeIsDeleted: Boolean = false,
) {
    companion object {
        const val TABLE_ID = 6405
    }
}
