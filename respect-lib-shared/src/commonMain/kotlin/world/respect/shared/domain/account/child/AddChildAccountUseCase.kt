package world.respect.shared.domain.account.child

import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest

interface AddChildAccountUseCase {
    suspend operator fun invoke(personInfo: RespectRedeemInviteRequest.PersonInfo)
}