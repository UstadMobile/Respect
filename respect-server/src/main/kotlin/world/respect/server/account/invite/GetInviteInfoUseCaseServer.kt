package world.respect.server.account.invite

import org.koin.core.component.KoinComponent
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase

class GetInviteInfoUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
): GetInviteInfoUseCase, KoinComponent {

    override suspend fun invoke(code: String): RespectInviteInfo {
        val clazz = schoolDb.getClassEntityDao().list(
            code = code
        ).firstOrNull() ?: throw IllegalArgumentException("class not found for code: $code")
            .withHttpStatus(404)

        return RespectInviteInfo(
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
}