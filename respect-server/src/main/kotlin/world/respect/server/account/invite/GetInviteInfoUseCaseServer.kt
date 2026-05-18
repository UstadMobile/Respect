package world.respect.server.account.invite

import org.koin.core.component.KoinComponent
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import kotlin.collections.first

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
        val familyPersonUid = invite.iForFamilyOfGuid

        val familyPerson = if(familyPersonUid != null) {
            schoolDb.getPersonEntityDao().findByGuidNum(uidNumberMapper(familyPersonUid))
        } else {
            null
        }

        val familyPersonName = if(familyPerson != null) {
            buildString {
                append(familyPerson.person.pGivenName)
                append(" ")
                familyPerson.person.pMiddleName?.also {
                    append(it)
                    append(" ")
                }
                append(familyPerson.person.pFamilyName)
            }
        } else {
            null
        }

        val familyPersonRole = familyPerson?.roles?.first { it.prIsPrimaryRole }?.prRoleEnum

        return RespectInviteInfo(
            className = className,
            familyPersonName = familyPersonName,
            familyPersonRole = familyPersonRole,
            invite = invite.toModel(),
        )
    }
}