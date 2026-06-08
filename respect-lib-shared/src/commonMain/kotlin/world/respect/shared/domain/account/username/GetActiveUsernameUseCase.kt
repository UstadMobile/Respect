package world.respect.shared.domain.account.username

import kotlinx.coroutines.flow.first
import world.respect.shared.domain.account.RespectAccountManager

class GetActiveUsernameUseCase(
    private val accountManager: RespectAccountManager,
) {
    suspend operator fun invoke(): String {
        accountManager.activeAccount
            ?: throw IllegalStateException("No active account")

        val sessionAndPerson = accountManager.selectedAccountAndPersonFlow
            .first { it != null }
            ?: throw IllegalStateException("No active session and person")

        return sessionAndPerson.person.username
            ?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(
                sessionAndPerson.person.givenName.takeIf { it.isNotBlank() },
                sessionAndPerson.person.familyName.takeIf { it.isNotBlank() },
            ).joinToString(" ").takeIf { it.isNotBlank() }
            ?: sessionAndPerson.person.guid
    }
}