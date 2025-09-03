package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class PersonParentJoin(

    @PrimaryKey(autoGenerate = true)
    var ppjUid: Long = 0,

    var ppjPcsn: Long = 0,

    var ppjLcsn: Long = 0,

    var ppjLcb: Int = 0,

    var ppjLct: Long = 0,

    /**
     * The personUid of the parent or legal guardian
     */
    var ppjParentPersonUid: Long = 0,

    /**
     * The personUid of the minor (e.g. the child)
     */
    var ppjMinorPersonUid: Long = 0,

    /**
     * The relationship type e.g. mother, father, other legal guardian
     */
    var ppjRelationship: Int = 0,

    var ppjEmail: String? = null,

    var ppjPhone: String? = null,

    var ppjInactive: Boolean = false,

    var ppjStatus: Int = 0,

    var ppjApprovalTiemstamp: Long = 0,

    var ppjApprovalIpAddr: String? = null,
) {


    companion object {

        const val TABLE_ID = 512

        const val STATUS_UNSET = 0

        const val STATUS_APPROVED = 1

        const val STATUS_REJECTED = 2

        const val RELATIONSHIP_MOTHER = 1

        const val RELATIONSHIP_FATHER = 2

        const val RELATIONSHIP_OTHER_LEGAL_GUARDIAN = 4

        const val RELATIONSHIP_OTHER = 8

        const val RELATIONSHIP_ACCOUNT_OWNER = 5
    }

}