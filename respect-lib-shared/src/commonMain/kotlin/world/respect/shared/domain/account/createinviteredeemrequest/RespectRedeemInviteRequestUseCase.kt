package world.respect.shared.domain.account.createinviteredeemrequest

import kotlinx.datetime.LocalDate
import world.respect.datalayer.oneroster.model.OneRosterGenderEnum
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.respect.model.invite.RespectRedeemInviteRequest

class RespectRedeemInviteRequestUseCase {

    operator fun invoke(
         inviteInfo : RespectInviteInfo,
         username : String
    ): RespectRedeemInviteRequest {
        val account = RespectRedeemInviteRequest.Account(
            username = username,
            credential = "dummyCredential"
        )
        return RespectRedeemInviteRequest(
            inviteInfo = inviteInfo,
            student = RespectRedeemInviteRequest.PersonInfo(
                name = "Student Name",
                gender = OneRosterGenderEnum.MALE,
                dateOfBirth = LocalDate.parse("2010-01-01")
            ),
            parentOrGuardian = RespectRedeemInviteRequest.PersonInfo(
                name = "Parent Name",
                gender = OneRosterGenderEnum.FEMALE,
                dateOfBirth = LocalDate.parse("1980-05-05")
            ),
            parentOrGuardianRole = RespectRedeemInviteRequest.GuardianRole.MOTHER,
            account = account
        )
    }
}
