package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    indices = arrayOf(
        Index("spToPersonUid", name = "idx_systempermission_personuid")
    )
)

/**
 * @param spPermissionsFlag Permission Flag as per PermissionFlags
 */
@Serializable
data class SystemPermission(
    @PrimaryKey(autoGenerate = true)
    var spUid: Long = 0,

    var spToPersonUid: Long = 0,

    var spToGroupUid: Long = 0,

    var spPermissionsFlag: Long = 0,

    var spLastModified: Long = 0,

    var spIsDeleted: Boolean = false,
) {

    companion object {

        const val PERSON_DEFAULT_PERMISSIONS = 0L

        const val TABLE_ID = 10011
    }


}