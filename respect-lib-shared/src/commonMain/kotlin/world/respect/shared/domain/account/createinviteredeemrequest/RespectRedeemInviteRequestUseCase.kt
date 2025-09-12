package world.respect.shared.domain.account.createinviteredeemrequest

import kotlinx.datetime.LocalDate
import world.respect.datalayer.oneroster.model.OneRosterGenderEnum
import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.credentials.passkey.util.toGuardianRole
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.shared.viewmodel.manageuser.profile.ProfileType

class RespectRedeemInviteRequestUseCase {
    operator fun invoke(
        inviteInfo: RespectInviteInfo,
        username: String,
        type: ProfileType,
        personInfo: RespectRedeemInviteRequest.PersonInfo,
        credential: RespectRedeemInviteRequest.RedeemInviteCredential
    ): RespectRedeemInviteRequest {
        val account = RespectRedeemInviteRequest.Account(
            username = username,
            credential = credential
        )

        return when (type) {
            ProfileType.STUDENT, ProfileType.CHILD -> RespectRedeemInviteRequest(
                inviteInfo = inviteInfo,
                student = personInfo,
                parentOrGuardian = null,
                parentOrGuardianRole = null,
                account = account
            )
            ProfileType.PARENT -> RespectRedeemInviteRequest(
                inviteInfo = inviteInfo,
                student = RespectRedeemInviteRequest.PersonInfo(),
                parentOrGuardian = personInfo,
                parentOrGuardianRole = personInfo.gender?.toGuardianRole(),
                account = account
            )
        }
    }
}
