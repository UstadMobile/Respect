package world.respect.shared.domain.account.invite

import io.ktor.http.Url
import org.koin.core.component.KoinComponent
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.Invite2
import world.respect.shared.domain.navigation.deeplink.UrlToCustomDeepLinkUseCase

class CreateInviteUseCaseDataSource(
    private val schoolUrl : Url,
    private val urlToCustomDeepLinkUseCase: UrlToCustomDeepLinkUseCase,
    private val schoolDataSource: SchoolDataSource,
) : CreateInviteUseCase, KoinComponent {

    override suspend fun invoke(invite: Invite2): String {
        TODO("Not yet implemented")
    }
}
