package world.respect.shared.domain.account.child

import io.github.aakira.napier.Napier
import org.koin.core.component.KoinComponent
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.ext.dataOrNull
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.util.toPerson

class AddChildAccountUseCaseDataSource(
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val schoolDataSource: SchoolDataSource,
) : AddChildAccountUseCase, KoinComponent {

    override suspend operator fun invoke(
        personInfo: RespectRedeemInviteRequest.PersonInfo,
        parentUsername: String,
        classUid: String,
        inviteCode: String
    ) {
        Napier.d("AddChildAccountUseCase: adding child ${personInfo.name} for parent $parentUsername")
        val parentPerson = schoolDataSource.personDataSource.findByGuid(
            loadParams = DataLoadParams(),
            guid = authenticatedUser.guid,
        ).dataOrNull() ?: throw IllegalStateException("Parent person not found: $authenticatedUser")

        val childUid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
            Person.TABLE_ID
        ).toString()

        val parentWithRel = parentPerson.copy(
            relatedPersonUids = parentPerson.relatedPersonUids + childUid
        )

        val childPerson = personInfo.toPerson(
            role = PersonRoleEnum.STUDENT,
            guid = childUid
        ).copy(
            relatedPersonUids = listOf(authenticatedUser.guid)
        )

        val enrollment = Enrollment(
            uid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(Enrollment.TABLE_ID)
                .toString(),
            classUid = classUid,
            personUid = childUid,
            role = EnrollmentRoleEnum.PENDING_STUDENT,
            inviteCode = inviteCode,
        )

        schoolDataSource.personDataSource.store(
            listOf(parentWithRel, childPerson)
        )

        schoolDataSource.enrollmentDataSource.store(listOf(enrollment))
    }
}