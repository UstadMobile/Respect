package world.respect.shared.domain.mock

import world.respect.datalayer.respect.model.invite.RespectPendingInviteState
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.shared.domain.account.invite.SubmitRedeemInviteRequestUseCase

class MockSubmitRedeemInviteRequestUseCase : SubmitRedeemInviteRequestUseCase {
    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest
    ): RespectPendingInviteState {
        TODO()
    }
}
