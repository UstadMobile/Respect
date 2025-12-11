package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.InviteEntity
import world.respect.datalayer.school.model.Invite
import world.respect.libutil.util.time.systemTimeInMillis
import kotlin.time.Instant


data class InviteEntities(
    val inviteEntity: InviteEntity
)
fun InviteEntity.toModel(): Invite {
    return Invite(
        guid = iGuid,
        code = iCode,
        newRole = iNewRole,
        forFamilyOfGuid = iForFamilyOfGuid,
        forClassGuid = iForClassGuid,
        forClassRole = iForClassRole,
        forClassName = iForClassName,
        schoolName = iSchoolName,
        inviteMultipleAllowed = iInviteMultipleAllowed,
        approvalRequired = iApprovalRequired,
        expiration = iExpiration,
        lastModified = Instant.fromEpochMilliseconds(iLastModified),
        stored = Instant.fromEpochMilliseconds(systemTimeInMillis()),
        inviteStatus = iInviteStatus
    )
}

fun InviteEntities.toModel(): Invite {
    return Invite(
        guid = inviteEntity.iGuid,
        code = inviteEntity.iCode,
        newRole = inviteEntity.iNewRole,
        forFamilyOfGuid = inviteEntity.iForFamilyOfGuid,
        forClassGuid = inviteEntity.iForClassGuid,
        forClassName = inviteEntity.iForClassName,
        schoolName = inviteEntity.iSchoolName,
        forClassRole = inviteEntity.iForClassRole,
        inviteMultipleAllowed = inviteEntity.iInviteMultipleAllowed,
        approvalRequired = inviteEntity.iApprovalRequired,
        expiration = inviteEntity.iExpiration,
        inviteStatus = inviteEntity.iInviteStatus,
        lastModified = Instant.fromEpochMilliseconds(inviteEntity.iLastModified),
        stored = Instant.fromEpochMilliseconds(systemTimeInMillis())
    )
}

fun Invite.toEntities(uidNumberMapper: UidNumberMapper): InviteEntities {
    val guidHash = uidNumberMapper(guid)
    val familyHash = forFamilyOfGuid?.let { uidNumberMapper(it) }
    val classHash = forClassGuid?.let { uidNumberMapper(it) }

    return InviteEntities(
        inviteEntity = InviteEntity(
            iGuid = guid,
            iGuidHash = guidHash,
            iCode = code,
            iNewRole = newRole,
            iForFamilyOfGuid = forFamilyOfGuid,
            iForFamilyOfGuidHash = familyHash,
            iForClassGuid = forClassGuid,
            iForClassName = forClassName,
            iSchoolName = schoolName,
            iForClassGuidHash = classHash,
            iForClassRole = forClassRole,
            iInviteMultipleAllowed = inviteMultipleAllowed,
            iApprovalRequired = approvalRequired,
            iLastModified = lastModified.toEpochMilliseconds(),
            iExpiration = expiration,
            iInviteStatus =inviteStatus
        )
    )
}
