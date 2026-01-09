package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.libutil.util.time.systemTimeInMillis

@Entity
data class InviteEntity(
    val iGuid: String,
    @PrimaryKey
    val iGuidHash: Long,
    val iCode: String,
    val iNewRole: PersonRoleEnum? = null,
    val iForFamilyOfGuid: String? = null,
    val iForFamilyOfGuidHash: Long? = null,
    val iForClassGuid: String? = null,
    val iForClassName: String? = null,
    val iSchoolName: String? = null,
    val iForClassGuidHash: Long? = null,
    val iForClassRole: EnrollmentRoleEnum ?=null,
    val iInviteMultipleAllowed: Boolean = false,

    val iIsFirstUser: Boolean = false,
    val iApprovalRequired: Boolean = false,
    val iLastModified: Long = systemTimeInMillis(),
    val iExpiration: Long = 0,
    var iInviteStatus: Int = STATUS_PENDING,
){
    companion object {
        const val STATUS_PENDING = 0
        const val STATUS_ACCEPTED = 1
        const val STATUS_REVOKED = 2
    }
}