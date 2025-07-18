package world.respect.shared.domain.mock

import world.respect.datalayer.respect.model.invite.RespectPendingInviteState
import world.respect.datalayer.respect.model.invite.RespectRedeemInviteRequest
import world.respect.shared.domain.account.invite.SubmitRedeemInviteRequestUseCase

class MockSubmitRedeemInviteRequestUseCase : SubmitRedeemInviteRequestUseCase {
    override suspend fun invoke(
        redeemRequest: RespectRedeemInviteRequest
    ): RespectPendingInviteState {
        return RespectPendingInviteState(
            uid = "mock-uid-123",
            request = redeemRequest,
            userSourcedId = "user-abc",
            status = RespectPendingInviteState.Status.PENDING,
            deciderUserSourcedId = null,
            lastModified = System.currentTimeMillis()
        )
    }
}
