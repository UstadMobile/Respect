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
fun Invite2.copyWithNewApprovalRequiredAfter(
    approvalRequiredAfter: Instant,
    lastModified: Instant = Clock.System.now(),
): Invite2 {
    return when(this) {
        is NewUserInvite -> copy(
            approvalRequiredAfter = approvalRequiredAfter,
            lastModified = lastModified
        )
        is FamilyMemberInvite -> copy(
            approvalRequiredAfter = approvalRequiredAfter,
            lastModified = lastModified
        )
        is ClassInvite -> copy(
            approvalRequiredAfter = approvalRequiredAfter,
            lastModified = lastModified
        )
    }
}

fun Invite2.isApprovalRequiredNow(): Boolean = approvalRequiredAfter < Clock.System.now()
