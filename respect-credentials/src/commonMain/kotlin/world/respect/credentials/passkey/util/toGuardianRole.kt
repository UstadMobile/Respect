package world.respect.credentials.passkey.util

import world.respect.credentials.passkey.RespectRedeemInviteRequest
import world.respect.datalayer.oneroster.model.OneRosterGenderEnum

fun OneRosterGenderEnum.toGuardianRole(): RespectRedeemInviteRequest.GuardianRole =
    when (this) {
        OneRosterGenderEnum.FEMALE -> RespectRedeemInviteRequest.GuardianRole.MOTHER
        OneRosterGenderEnum.MALE -> RespectRedeemInviteRequest.GuardianRole.FATHER
        OneRosterGenderEnum.OTHER,
        OneRosterGenderEnum.UNSPECIFIED -> RespectRedeemInviteRequest.GuardianRole.OTHER_GUARDIAN
    }