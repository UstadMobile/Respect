package world.respect.shared.domain.account.invite

import world.respect.datalayer.respect.model.invite.RespectPendingInviteState
import world.respect.credentials.passkey.RespectRedeemInviteRequest


/**
 * Submits a RespectRedeemInviteRequest (using information retrieved from GetInviteUseCase and info
 * the user fills in the UI). This results in the creation of a temporary account awaiting activation.
 */
interface SubmitRedeemInviteRequestUseCase {

    /**
     *
     */
    suspend operator fun invoke(
        redeemRequest: RespectRedeemInviteRequest
    ): RespectPendingInviteState

}