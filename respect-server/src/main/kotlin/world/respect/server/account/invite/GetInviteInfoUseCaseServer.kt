package world.respect.server.account.invite

import org.koin.core.component.KoinComponent
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase

class GetInviteInfoUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
) : GetInviteInfoUseCase, KoinComponent {

    override suspend fun invoke(code: String): RespectInviteInfo {

        val invite = schoolDb.getInviteEntityDao().getInviteByInviteCode(code)
            ?: throw IllegalArgumentException("invite not found for code: $code")
                .withHttpStatus(404)

        val classUid = invite.iForClassGuid
        val className = if(classUid != null) {
            schoolDb.getClassEntityDao().findByGuid(uidNumberMapper(classUid))
                ?.clazz?.cTitle
        }else {
            null
        }
        val childUid = invite.iForFamilyOfGuid
        val childName = if(childUid != null) {
           val child = schoolDb.getPersonEntityDao().findByGuidNum(uidNumberMapper(childUid))
            buildString {
                append(child?.person?.pGivenName)
                append(" ")
                child?.person?.pMiddleName?.also {
                    append(it)
                    append(" ")
                }
                append(child?.person?.pFamilyName)
            }
        }else {
            null
        }
        return RespectInviteInfo(
            className = className,
            childName = childName,
            invite = invite.toModel(),
        )
    }
}