package world.respect.server.account.invite

import io.ktor.http.Url
import org.koin.core.component.KoinComponent
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.school.model.Invite
import world.respect.shared.domain.account.invite.CreateInviteUseCase

class CreateInviteUseCaseServer(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val schoolUrl : Url
) : CreateInviteUseCase, KoinComponent {

    override suspend fun invoke(invite: Invite): String {

        val inviteEntity = invite.toEntities(uidNumberMapper).inviteEntity
        schoolDb.getInviteEntityDao().insert(inviteEntity)
        return buildString {
            append(schoolUrl.toString())
            append(invite.code)
        }

    }
}
