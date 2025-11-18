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
    val iForClassGuidHash: Long? = null,
    val iForClassRole: EnrollmentRoleEnum ?=null,
    val iInviteMultipleAllowed: Boolean = false,
    val iApprovalRequired: Boolean = false,
    val iLastModified: Long = systemTimeInMillis(),
    val iExpiration: Long? = null,
)