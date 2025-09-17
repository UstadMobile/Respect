package world.respect.shared.domain.account.createinviteredeemrequest

import kotlinx.datetime.LocalDate
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.respect.model.invite.RespectRedeemInviteRequest
import world.respect.datalayer.school.model.PersonGenderEnum

class RespectRedeemInviteRequestUseCase {

    operator fun invoke(
         inviteInfo : RespectInviteInfo,
         username : String
    ): RespectRedeemInviteRequest {
        TODO()
    }
//        val account = RespectRedeemInviteRequest.Account(
//            username = username,
//            credential = "dummyCredential"
//        )
//        return RespectRedeemInviteRequest(
//            inviteInfo = inviteInfo,
//            accountPersonInfo = RespectRedeemInviteRequest.PersonInfo(
//                name = "Student Name",
//                gender = PersonGenderEnum.MALE,
//                dateOfBirth = LocalDate.parse("2010-01-01")
//            ),
//            studentPersonInfo = RespectRedeemInviteRequest.PersonInfo(
//                name = "Parent Name",
//                gender = PersonGenderEnum.FEMALE,
//                dateOfBirth = LocalDate.parse("1980-05-05")
//            ),
//            parentOrGuardianRole = RespectRedeemInviteRequest.GuardianRole.MOTHER,
//            account = account,
//            isParent = true,
//        )
//    }
}
