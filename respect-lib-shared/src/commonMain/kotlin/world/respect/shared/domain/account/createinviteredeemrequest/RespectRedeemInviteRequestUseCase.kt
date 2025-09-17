package world.respect.shared.domain.account.createinviteredeemrequest

import kotlinx.datetime.LocalDate
import world.respect.datalayer.oneroster.model.OneRosterGenderEnum
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.credentials.passkey.util.toGuardianRole
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.respect.model.invite.RespectRedeemInviteRequest
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

class RespectRedeemInviteRequestUseCase {
    operator fun invoke(
        inviteInfo: RespectInviteInfo,
        username: String,
        personInfo: RespectRedeemInviteRequest.PersonInfo,
        parentOrGuardian: RespectRedeemInviteRequest.PersonInfo?,
        credential: RespectRedeemInviteRequest.RedeemInviteCredential
    ): RespectRedeemInviteRequest {
        val account = RespectRedeemInviteRequest.Account(
            username = username,
            credential = credential
        )

        return RespectRedeemInviteRequest(
            inviteInfo = inviteInfo,
            student =personInfo,
            parentOrGuardian = parentOrGuardian,
            parentOrGuardianRole = parentOrGuardian?.gender?.toGuardianRole(),
            account = account
        )
    }
}
