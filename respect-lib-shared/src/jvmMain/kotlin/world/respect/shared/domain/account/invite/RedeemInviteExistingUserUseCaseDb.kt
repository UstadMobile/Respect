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
import world.respect.datalayer.school.ext.copyWithClassName
import world.respect.datalayer.school.ext.copyWithInviteInfo
import world.respect.datalayer.school.ext.isApprovalRequiredNow
import world.respect.datalayer.school.ext.primaryRole
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.FamilyMemberInvite
import world.respect.datalayer.school.model.PersonRoleEnum
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
                }
                ?: throw IllegalArgumentException("existing person not found for guid: $accountGuid")
                    .withHttpStatus(404)

        schoolDataSource.personDataSource.updateLocal(
            listOf(accountPerson),
            forceOverwrite = true
        )

        val enrollmentRole = inviteFromDb.accepterEnrollmentRole(approvalRequired)
        if (enrollmentRole != null && inviteFromDb is ClassInvite) {
            val primaryRole = accountPerson.primaryRole()

            val invalidInvite =
                (primaryRole == PersonRoleEnum.TEACHER && enrollmentRole == EnrollmentRoleEnum.STUDENT) ||
                (primaryRole == PersonRoleEnum.TEACHER && enrollmentRole == EnrollmentRoleEnum.PENDING_STUDENT) ||
                (primaryRole == PersonRoleEnum.STUDENT && enrollmentRole == EnrollmentRoleEnum.TEACHER) ||
                (primaryRole == PersonRoleEnum.STUDENT && enrollmentRole == EnrollmentRoleEnum.PENDING_TEACHER)


            if (invalidInvite) {
                throw IllegalArgumentException(
                    "Sorry. Invalid invitation: not available for your user type."
                ).withHttpStatus(400)
            }
            val className = schoolDb.getClassEntityDao()
                .findByGuid(uidNumberMapper(inviteFromDb.classUid))?.clazz?.cTitle

            schoolDataSource.enrollmentDataSource.updateLocal(
                listOf(
                    Enrollment(
                        uid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
                            Enrollment.TABLE_ID
                        ).toString(),
                        classUid = inviteFromDb.classUid,
                        personUid = if (inviteFromDb.inviteMode == ClassInviteModeEnum.VIA_PARENT) selectedChildGuid
                            ?: "" else accountPerson.guid,
                        role = enrollmentRole,
                        beginDate = Clock.System.now().toLocalDateTime(
                            TimeZone.currentSystemDefault()
                        ).date
                    ).let {
                        if (className != null) {
                            it.copyWithClassName(className)
                        } else {
                            it
                        }
                    }

                )
            )
        }
        if (inviteFromDb is FamilyMemberInvite) {

            val parentUid = inviteFromDb.personUid
            val childUid = selectedChildGuid ?: accountPerson.guid

            val parentPerson =
                schoolDb.getPersonEntityDao()
                    .findByGuidNum(uidNumberMapper(parentUid))
                    ?.toPersonEntities()
                    ?.toModel()
                    ?: throw IllegalStateException("Parent not found: $parentUid")

            val childPerson =
                schoolDb.getPersonEntityDao()
                    .findByGuidNum(uidNumberMapper(childUid))
                    ?.toPersonEntities()
                    ?.toModel()
                    ?: throw IllegalStateException("Child not found: $childUid")

            val updatedParent = parentPerson.copy(
                relatedPersonUids = parentPerson.relatedPersonUids + childUid,
                lastModified = timeNow
            )

            val updatedChild = childPerson.copy(
                relatedPersonUids = childPerson.relatedPersonUids + parentUid,
                lastModified = timeNow
            )

            schoolDataSource.personDataSource.updateLocal(
                listOf(updatedParent, updatedChild),
                forceOverwrite = true
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