package world.respect.shared.domain.account.child

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.lib.primarykeygen.PrimaryKeyGenerator
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.util.toPerson
import kotlin.time.Clock

class AddChildAccountUseCase(
    private val schoolDataSource: SchoolDataSource,
    private val primaryKeyGenerator: PrimaryKeyGenerator
) {
     suspend operator fun invoke(
        personInfo: RespectRedeemInviteRequest.PersonInfo,
        parentUsername: String,
        classUid: String,
        inviteCode: String
    ) {

        val accountGuid =
            primaryKeyGenerator.nextId(Person.TABLE_ID).toString()

        val parentPerson = schoolDataSource.personDataSource.findByUsername(parentUsername)
        if (parentPerson == null) {
            throw Exception("Parent person not found").withHttpStatus(404)
        }

         val childPerson = personInfo.toPerson(
             role = PersonRoleEnum.STUDENT,
             guid = accountGuid
         ).copy(relatedPersonUids = listOf(parentPerson.guid))

        val updatedParent = parentPerson.copy(
            relatedPersonUids = parentPerson.relatedPersonUids + accountGuid,
            lastModified = Clock.System.now()
        )

        schoolDataSource.personDataSource.store(listOf(updatedParent,childPerson))

        val newEnrollment = Enrollment(
            uid = primaryKeyGenerator.nextId(Enrollment.TABLE_ID).toString(),
            classUid = classUid,
            personUid = childPerson.guid,
            role = EnrollmentRoleEnum.PENDING_STUDENT,
            inviteCode = inviteCode,
        )
        schoolDataSource.enrollmentDataSource.store(listOf(newEnrollment))
    }
}