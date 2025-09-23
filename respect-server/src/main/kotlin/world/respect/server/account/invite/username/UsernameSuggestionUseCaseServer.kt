package world.respect.server.account.invite.username

import org.koin.core.component.KoinComponent
import world.respect.datalayer.school.PersonDataSource
import world.respect.libutil.util.throwable.withHttpStatus
import world.respect.shared.domain.account.username.UsernameSuggestionUseCase
import world.respect.shared.domain.account.username.filterusername.FilterUsernameUseCase

class UsernameSuggestionUseCaseServer(
    private val filterUsernameUseCase: FilterUsernameUseCase,
    private val personDb: PersonDataSource,
): UsernameSuggestionUseCase, KoinComponent  {

    companion object {
        private const val MAX_ATTEMPTS = 1000
    }

    override suspend operator fun invoke(name: String): String {
        val baseUsername = filterUsernameUseCase(name, "")
        var suggestedUsername: String

        for (index in 0 until MAX_ATTEMPTS) {
            suggestedUsername = if (index == 0) baseUsername else "$baseUsername$index"
            if (personDb.findByUsername(suggestedUsername) == null) {
                return suggestedUsername
            }
        }

        throw IllegalArgumentException("Unable to generate a unique username")
            .withHttpStatus(404)
    }
}
