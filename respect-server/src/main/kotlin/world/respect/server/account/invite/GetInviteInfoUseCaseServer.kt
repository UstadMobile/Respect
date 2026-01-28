package world.respect.server.account.invite

import org.koin.core.component.KoinComponent
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase

class GetInviteInfoUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
) : GetInviteInfoUseCase, KoinComponent {

    override suspend fun invoke(code: String): RespectInviteInfo {

        val invite = schoolDb.getInviteEntityDao().getInviteByInviteCode(code)
            ?: throw IllegalArgumentException("invite not found for code: $code")
                .withHttpStatus(404)

        return RespectInviteInfo(
            classGuid = invite.iForClassGuid,
            className = invite.iForClassName,
            invite = invite.toModel(),
        )


    }
}