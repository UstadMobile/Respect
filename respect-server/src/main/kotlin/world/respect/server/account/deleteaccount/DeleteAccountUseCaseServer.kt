package world.respect.server.account.deleteaccount

import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSource
import world.respect.shared.domain.account.deleteaccount.DeleteAccountUseCase

class DeleteAccountUseCaseServer(
    private val schoolDataSource: SchoolDataSource,
    private val authenticatedUser: AuthenticatedUserPrincipalId
) : DeleteAccountUseCase {

    override suspend fun invoke(): Boolean {
        return schoolDataSource
            .personDataSource
            .delete(authenticatedUser.guid)
    }
}
