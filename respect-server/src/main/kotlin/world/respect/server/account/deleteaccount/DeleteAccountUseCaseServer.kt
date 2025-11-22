package world.respect.server.account.deleteaccount

import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.shared.domain.account.deleteaccount.DeleteAccountUseCase

class DeleteAccountUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId
) : DeleteAccountUseCase {

    override suspend fun invoke(): Boolean {

        val personDao = schoolDb.getPersonEntityDao()
        val rowsDeleted = personDao.deletePerson(authenticatedUser.guid)
        return rowsDeleted > 0

    }
}
