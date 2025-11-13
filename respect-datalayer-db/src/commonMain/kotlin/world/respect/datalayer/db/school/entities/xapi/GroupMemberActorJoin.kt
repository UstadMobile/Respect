package world.respect.datalayer.db.school.entities.xapi

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable

/**
 * See XapiGroup.toGroupEntities for further details on the mapping between the Xapi Spec and the
 * database entities.
 */
@Entity(
    primaryKeys = ["gmajGroupActorUid", "gmajMemberActorUid"],
    indices = [Index("gmajGroupActorUid", name = "idx_groupmemberactorjoin_gmajgroupactoruid"), Index("gmajMemberActorUid", name = "idx_groupmemberactorjoin_gmajmemberactoruid")]
)
@Serializable
data class GroupMemberActorJoin(
    var gmajGroupActorUid: Long = 0,

    var gmajMemberActorUid: Long = 0,

    var gmajLastMod: Long = 0,
) {
    companion object {
        const val TABLE_ID = 4232
    }
}
