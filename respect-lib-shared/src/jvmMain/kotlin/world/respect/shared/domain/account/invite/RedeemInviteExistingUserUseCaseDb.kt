package world.respect.shared.domain.account.invite

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.datalayer.school.ext.accepterEnrollmentRole
import world.respect.datalayer.school.ext.copyWithInviteInfo
import world.respect.datalayer.school.ext.isApprovalRequiredNow
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.Enrollment
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import kotlin.time.Clock

class RedeemInviteExistingUserUseCaseDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator,
    private val schoolDataSource: SchoolDataSourceLocal,
) : RedeemInviteExistingUserUseCase, KoinComponent {

    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest,
        selectedChildGuid: String?
    ) {
        val timeNow = Clock.System.now()

        val inviteFromDb = schoolDb.getInviteEntityDao().getInviteByInviteCode(
            redeemRequest.code
        )?.toModel()
            ?: throw IllegalArgumentException("invite not found for code: ${redeemRequest.code}")
                .withHttpStatus(404)

        val approvalRequired = inviteFromDb.isApprovalRequiredNow()
        val accountGuid = redeemRequest.account.guid

        val accountPerson =
            schoolDb.getPersonEntityDao().findByGuidNum(uidNumberMapper(accountGuid))
                ?.toPersonEntities()
                ?.toModel()
                ?.let { person ->
                    if (approvalRequired) {
                        person.copyWithInviteInfo(invite = redeemRequest.invite)
                    } else {
                        person
                    }
                } ?: throw IllegalArgumentException("existing person not found for guid: $accountGuid")
                    .withHttpStatus(404)

        schoolDataSource.personDataSource.updateLocal(
            listOf(accountPerson),
            forceOverwrite = true
        )

        val enrollmentRole = inviteFromDb.accepterEnrollmentRole(approvalRequired)
        if (enrollmentRole != null && inviteFromDb is ClassInvite) {
            schoolDataSource.enrollmentDataSource.updateLocal(
                listOf(
                    Enrollment(
                        uid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
                            Enrollment.TABLE_ID
                        ).toString(),
                        classUid = inviteFromDb.classUid,
                        personUid = if (inviteFromDb.inviteMode == ClassInviteModeEnum.VIA_PARENT) selectedChildGuid?:"" else accountPerson.guid,
                        role = enrollmentRole,
                        beginDate = Clock.System.now().toLocalDateTime(
                            TimeZone.currentSystemDefault()
                        ).date
                    )
                )
            )
        }

        if (!selectedChildGuid.isNullOrBlank()) {
            val childPerson =
                schoolDb.getPersonEntityDao().findByGuidNum(uidNumberMapper(selectedChildGuid))
                    ?.toPersonEntities()
                    ?.toModel()
                    ?.copyWithInviteInfo(invite = redeemRequest.invite)
                    ?.copy(
                        lastModified = timeNow
                    )

            childPerson?.let {
                schoolDataSource.personDataSource.updateLocal(
                    listOf(it),
                    forceOverwrite = true
                )
            }
        }

        markFirstUserInviteAsDeleted(inviteFromDb, schoolDataSource)
    }
}