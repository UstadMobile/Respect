package world.respect.shared.domain.account.createinviteredeemrequest

import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.credentials.passkey.util.toGuardianRole
import world.respect.datalayer.respect.model.invite.RespectInviteInfo

class RespectRedeemInviteRequestUseCase {
    operator fun invoke(
        inviteInfo: RespectInviteInfo,
        username: String,
        personInfo: RespectRedeemInviteRequest.PersonInfo,
        parentOrGuardian: RespectRedeemInviteRequest.PersonInfo?,
        credential: RespectRedeemInviteRequest.RedeemInviteCredential
    ): RespectRedeemInviteRequest {
        /*
        val account = RedeemInviteRequest.Account(
            username = username,
            credential = credential
        )

        return world.respect.shared.domain.account.invite.RedeemInviteRequest(
            inviteInfo = inviteInfo,
            student = personInfo,
            parentOrGuardian = parentOrGuardian,
            parentOrGuardianRole = parentOrGuardian?.gender?.toGuardianRole(),
            account = account
        )
        */
        TODO()
    }
}
