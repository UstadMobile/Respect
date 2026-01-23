package world.respect.shared.domain.account.invite

import world.respect.datalayer.school.model.Invite2

/**
 * UseCase to create an invite on the server side when datasource is not yet available (eg first user)
 *
 */
interface CreateInviteUseCase {

    suspend operator fun invoke(invite: Invite2): String

}