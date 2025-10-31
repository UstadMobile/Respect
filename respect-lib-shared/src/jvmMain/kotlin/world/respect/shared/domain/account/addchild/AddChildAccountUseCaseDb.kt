package world.respect.shared.domain.account.addchild

import org.koin.core.component.KoinComponent
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.entities.EnrollmentEntity
import world.respect.datalayer.db.school.entities.PersonRelatedPersonEntity
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.StatusEnum
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.child.AddChildAccountUseCase
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.util.toPerson
import kotlin.time.Clock

class AddChildAccountUseCaseDb(
    private val schoolDb: RespectSchoolDatabase,
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator,

    private val uidNumberMapper: UidNumberMapper,
) : AddChildAccountUseCase, KoinComponent {
    override suspend operator fun invoke(
        personInfo: RespectRedeemInviteRequest.PersonInfo,
        parentUsername: String,
        classUid: String,
        inviteCode: String
    ) {
        val accountGuid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(Person.TABLE_ID).toString()

        val personEntityWithRoles = schoolDb.getPersonEntityDao().findByUsername(parentUsername)
            ?: throw Exception("Parent person not found").withHttpStatus(404)

        val parentGuid = personEntityWithRoles.person.pGuid

        val childPerson = personInfo.toPerson(
            role = PersonRoleEnum.STUDENT,
            guid = accountGuid
        ).copy(relatedPersonUids = listOf(parentGuid))

        val childPersonEntities = childPerson.toEntities(uidNumberMapper)
        val childPersonEntity = childPersonEntities.personEntity
        val roleEntities = childPersonEntities.personRoleEntities
        val relatedEntities = childPersonEntities.relatedPersonEntities

        val parentEntity = personEntityWithRoles.person
        val parentPersonUidNum = parentEntity.pGuidHash
        val addRelated = PersonRelatedPersonEntity(
            prpPersonUidNum = parentPersonUidNum,
            prpOtherPersonUid = accountGuid,
            prpOtherPersonUidNum = uidNumberMapper(accountGuid)
        )

        schoolDb.getPersonRelatedPersonEntityDao().upsert(listOf(addRelated))

        schoolDb.getPersonEntityDao().insert(childPersonEntity)
        if (roleEntities.isNotEmpty()) {
            schoolDb.getPersonRoleEntityDao().upsertList(roleEntities)
        }
        if (relatedEntities.isNotEmpty()) {
            schoolDb.getPersonRelatedPersonEntityDao().upsert(relatedEntities)
        }


        val classUidNum = uidNumberMapper(classUid)
        val enrollmentUid = schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(Enrollment.TABLE_ID).toString()
        val enrollmentUidNum = uidNumberMapper(enrollmentUid)
        val enrollmentEntity = EnrollmentEntity(
            eUid = enrollmentUid,
            eUidNum = enrollmentUidNum,
            eStatus = StatusEnum.ACTIVE,
            eLastModified = Clock.System.now(),
            eStored = Clock.System.now(),
            eClassUidNum = classUidNum,
            ePersonUidNum = uidNumberMapper(childPersonEntity.pGuid),
            eRole = EnrollmentRoleEnum.PENDING_STUDENT,
            eInviteCode = inviteCode,
        )

        schoolDb.getEnrollmentEntityDao().upsert(listOf(enrollmentEntity))

    }
}