package world.respect.shared.domain.account.invite

import world.respect.datalayer.school.model.Invite

interface CreateInviteUseCase {

    suspend operator fun invoke(invite: Invite): String
}