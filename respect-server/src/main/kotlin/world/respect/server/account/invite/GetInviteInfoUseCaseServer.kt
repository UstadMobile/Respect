package world.respect.server.account.invite

import org.koin.core.component.KoinComponent
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.school.model.Invite
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.libutil.util.time.systemTimeInMillis
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase

class GetInviteInfoUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
) : GetInviteInfoUseCase, KoinComponent {

    override suspend fun invoke(code: String): RespectInviteInfo {
        val clazz = schoolDb.getClassEntityDao().findByInviteCode(
            code = code
        ).firstOrNull() ?: throw IllegalArgumentException("class not found for code: $code")
            .withHttpStatus(404)

        val invite = schoolDb.getInviteEntityDao().getInviteByInviteCode(code)
            ?: throw IllegalArgumentException("invite not found for code: $code")
                .withHttpStatus(404)
        if (invite.iInviteStatus == Invite.STATUS_REVOKED) {
            throw IllegalArgumentException("invite is revoked")
                .withHttpStatus(400)
        }
        if (invite.iInviteStatus != Invite.STATUS_PENDING) {
            throw IllegalArgumentException("invite is already used")
                .withHttpStatus(400)
        }
        if (invite.iExpiration < systemTimeInMillis()) {
            throw IllegalArgumentException("invite is expired")
                .withHttpStatus(400)

        } else {
            return RespectInviteInfo(
                code = code,
                classGuid = invite.iForClassGuid,
                className = invite.iForClassName,
                invite = invite.toModel(),
            )
        }

    }
}