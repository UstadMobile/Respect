package world.respect.server.account.invite

import org.koin.core.component.KoinComponent
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.school.model.Invite
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.libutil.util.time.systemTimeInMillis
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase

class GetInviteInfoUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
) : GetInviteInfoUseCase, KoinComponent {

    override suspend fun invoke(code: String, type: Int?): RespectInviteInfo {
       val respectInviteInfo = when (type) {
            RespectInviteInfo.INVITE_TYPE_CLASS_CODE -> {
                val clazz = schoolDb.getClassEntityDao().list(
                    code = code
                ).firstOrNull() ?: throw IllegalArgumentException("class not found for code: $code")
                    .withHttpStatus(404)

                 RespectInviteInfo(
                    code = code,
                    classGuid = clazz.cGuid,
                    className = clazz.cTitle,
                    userInviteType = if (code == clazz.cTeacherInviteCode) {
                        RespectInviteInfo.UserInviteType.TEACHER
                    } else {
                        RespectInviteInfo.UserInviteType.STUDENT_OR_PARENT
                    }
                )
            }
            RespectInviteInfo.INVITE_TYPE_GENERIC -> {
                val invite = schoolDb.getInviteEntityDao().getInviteByInviteCode(code)
                    ?: throw IllegalArgumentException("invite not found for code: $code")
                    .withHttpStatus(404)
                if (invite.iInviteStatus == Invite.STATUS_REVOKED){
                    throw IllegalArgumentException("invite is revoked")
                        .withHttpStatus(400)
                }
                if (invite.iInviteStatus != Invite.STATUS_PENDING) {
                    throw IllegalArgumentException("invite is already used")
                        .withHttpStatus(400)
                }
                if (invite.iExpiration < systemTimeInMillis()){
                    throw IllegalArgumentException("invite is expired")
                        .withHttpStatus(400)

                }
                else{
                    RespectInviteInfo(
                        code = code,
                        classGuid = invite.iForClassGuid,
                        role = invite.iNewRole,
                        familyPersonGuid = invite.iForFamilyOfGuid,
                        classRole = invite.iForClassRole,
                        inviteMultipleAllowed = invite.iInviteMultipleAllowed,
                        approvalRequired = invite.iApprovalRequired,
                        className = "",
                    )
                }

            }

           else -> {  throw IllegalArgumentException("Unsupported invite type: $type")
               .withHttpStatus(400) }
       }
        return respectInviteInfo
    }
}