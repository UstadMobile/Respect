package world.respect.shared.domain.account.invite

import io.ktor.http.Url
import org.koin.core.component.KoinComponent
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.Invite
import world.respect.shared.domain.createlink.CreateLinkUseCase.Companion.PATH
import world.respect.shared.domain.createlink.CreateLinkUseCase.Companion.QUERY_PARAM
import world.respect.shared.domain.navigation.deeplink.UrlToCustomDeepLinkUseCase

class CreateInviteUseCaseDataSource(
    private val schoolUrl : Url,
    private val urlToCustomDeepLinkUseCase: UrlToCustomDeepLinkUseCase,
    private val schoolDataSource: SchoolDataSource,
    ) : CreateInviteUseCase, KoinComponent {
    override suspend fun invoke(invite: Invite): String {
        schoolDataSource.inviteDataSource.store(listOf(invite))
        val base = schoolUrl.toString().trimEnd('/')
        val url ="$base/$PATH?$QUERY_PARAM=${invite.code}"
        return urlToCustomDeepLinkUseCase(Url(url)).toString()
    }
}
