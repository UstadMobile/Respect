package world.respect.server.account.deleteaccount

import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.shared.domain.account.deleteaccount.DeleteAccountUseCase

class DeleteAccountUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId
) : DeleteAccountUseCase {

    override suspend fun invoke(): Boolean {

        val dao = schoolDb.getPersonEntityDao()
        dao.deletePerson(authenticatedUser.guid)

        return true
    }
}
