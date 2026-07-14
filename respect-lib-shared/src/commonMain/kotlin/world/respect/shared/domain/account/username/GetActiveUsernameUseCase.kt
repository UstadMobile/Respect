package world.respect.shared.domain.account.username

import kotlinx.coroutines.flow.first
import world.respect.shared.domain.account.RespectAccountManager

class GetActiveUsernameUseCase(
    private val accountManager: RespectAccountManager,
) {
    suspend operator fun invoke(): String {
        val sessionAndPerson = accountManager.selectedAccountAndPersonFlow.first()
            ?: throw IllegalStateException("No active session and person")

        return sessionAndPerson.person.username
            ?: throw IllegalStateException(
                "Active person has no username: ${sessionAndPerson.person.guid}"
            )
    }
}