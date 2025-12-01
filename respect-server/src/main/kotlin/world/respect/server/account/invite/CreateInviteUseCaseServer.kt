package world.respect.server.account.invite

import io.ktor.http.Url
import org.koin.core.component.KoinComponent
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.school.model.Invite
import world.respect.shared.domain.account.invite.CreateInviteUseCase
import world.respect.shared.domain.createlink.CreateLinkUseCase.Companion.PATH
import world.respect.shared.domain.createlink.CreateLinkUseCase.Companion.QUERY_PARAM
import world.respect.shared.domain.navigation.deeplink.UrlToCustomDeepLinkUseCase

class CreateInviteUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val schoolUrl : Url,
    private val urlToCustomDeepLinkUseCase: UrlToCustomDeepLinkUseCase,
) : CreateInviteUseCase, KoinComponent {
    override suspend fun invoke(invite: Invite): String {

        val inviteEntity = invite.toEntities(uidNumberMapper).inviteEntity
        schoolDb.getInviteEntityDao().insert(inviteEntity)
        val base = schoolUrl.toString().trimEnd('/')
        val url ="$base/$PATH?$QUERY_PARAM=${invite.code}"
        return urlToCustomDeepLinkUseCase(Url(url)).toString()
    }
}
