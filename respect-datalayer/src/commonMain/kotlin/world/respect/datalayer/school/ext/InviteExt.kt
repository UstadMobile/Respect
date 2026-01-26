package world.respect.datalayer.school.ext

import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.FamilyMemberInvite
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.NewUserInvite
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Make a copy of the given invite with an updated approval required after time.
 */
fun Invite2.copyInvite(
    approvalRequiredAfter: Instant = this.approvalRequiredAfter,
    code: String = this.code,
    lastModified: Instant = this.lastModified,
): Invite2 {
    return when(this) {
        is NewUserInvite -> copy(
            approvalRequiredAfter = approvalRequiredAfter,
            code = code,
            lastModified = lastModified
        )
        is FamilyMemberInvite -> copy(
            approvalRequiredAfter = approvalRequiredAfter,
            code = code,
            lastModified = lastModified
        )
        is ClassInvite -> copy(
            approvalRequiredAfter = approvalRequiredAfter,
            code = code,
            lastModified = lastModified
        )
    }
}

fun Invite2.isApprovalRequiredNow(): Boolean = approvalRequiredAfter < Clock.System.now()
