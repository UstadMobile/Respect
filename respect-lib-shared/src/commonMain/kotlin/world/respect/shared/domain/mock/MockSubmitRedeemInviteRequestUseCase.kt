package world.respect.shared.domain.mock

import world.respect.datalayer.respect.model.invite.RespectRedeemInviteRequest
import world.respect.shared.domain.account.AuthResponse
import world.respect.shared.domain.account.invite.RedeemInviteUseCase

class MockSubmitRedeemInviteRequestUseCase : RedeemInviteUseCase {
    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest
    ): AuthResponse {
        TODO()
    }
}
