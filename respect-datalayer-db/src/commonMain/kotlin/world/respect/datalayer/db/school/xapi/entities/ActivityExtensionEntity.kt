package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity

@Entity(primaryKeys = ["aeeActivityUid", "aeeKeyHash"])
data class ActivityExtensionEntity(
    val aeeActivityUid: Long = 0,

    val aeeKeyHash: Long = 0,

    val aeeKey: String? = null,

    val aeeJson: String? = null,

    val aeeLastMod: Long = 0,

    val aeeIsDeleted: Boolean = false,
) {
    companion object {
        const val TABLE_ID = 6405
    }
}
