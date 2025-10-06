package world.respect.shared.domain.account.child

import io.ktor.http.Url
import org.koin.core.component.KoinComponent
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.util.di.SchoolDataSourceLocalProvider
import world.respect.shared.util.toPerson
import kotlin.time.Clock

class AddChildAccountUseCaseDb(
    private val schoolUrl: Url,
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator,
    private val schoolDataSource: SchoolDataSourceLocalProvider
) : AddChildAccountUseCase, KoinComponent {


    override suspend fun invoke(
        personInfo: RespectRedeemInviteRequest.PersonInfo,
        parentUsername: String
    ) {

        val accountGuid =
            schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(Person.TABLE_ID).toString()
        val schoolDataSourceVal = schoolDataSource(
            schoolUrl = schoolUrl, AuthenticatedUserPrincipalId(accountGuid)
        )
        val parentPerson = schoolDataSourceVal.personDataSource.findByUsername(parentUsername)
        if (parentPerson==null){
            throw Exception("Parent person not found").withHttpStatus(404)
        }

        val updatedParent = parentPerson.copy(
            relatedPersonUids = parentPerson.relatedPersonUids + accountGuid,
            lastModified = Clock.System.now()
        )

        val childPerson = personInfo.toPerson(
            role = PersonRoleEnum.STUDENT,
            guid = accountGuid
        ).copy(relatedPersonUids = listOf(parentPerson.guid))

        schoolDataSourceVal.personDataSource.updateLocal(listOf(childPerson, updatedParent),true)

    }
}