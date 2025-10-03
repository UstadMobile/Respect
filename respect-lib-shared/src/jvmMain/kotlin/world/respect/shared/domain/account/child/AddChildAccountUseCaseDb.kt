package world.respect.shared.domain.account.child

import io.ktor.http.Url
import org.koin.core.component.KoinComponent
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.util.di.SchoolDataSourceLocalProvider
import world.respect.shared.util.toPerson

class AddChildAccountUseCaseDb(
    private val schoolUrl: Url,
    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator,
    private val schoolDataSource: SchoolDataSourceLocalProvider
) : AddChildAccountUseCase, KoinComponent {


    override suspend fun invoke(
        personInfo: RespectRedeemInviteRequest.PersonInfo,
    ) {

        val accountGuid =
            schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(Person.TABLE_ID).toString()

        val accountPerson = personInfo.toPerson(
            role = PersonRoleEnum.STUDENT,
            guid = accountGuid
        )

        val schoolDataSourceVal = schoolDataSource(
            schoolUrl = schoolUrl, AuthenticatedUserPrincipalId(accountGuid)
        )
        schoolDataSourceVal.personDataSource.updateLocal(listOf(accountPerson))
    }
}