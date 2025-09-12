package world.respect.credentials.passkey

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.datalayer.oneroster.model.OneRosterGenderEnum
import world.respect.datalayer.respect.model.invite.RespectInviteInfo

@Serializable
class RespectRedeemInviteRequest(
    val inviteInfo: RespectInviteInfo,
    val student: PersonInfo,
    val parentOrGuardian: PersonInfo?,
    val parentOrGuardianRole: GuardianRole?,
    val account: Account,
) {

    @Serializable
    enum class GuardianRole {
        FATHER, MOTHER, OTHER_GUARDIAN
    }

   @Serializable
   data class PersonInfo(
       val name: String ?=null,
       val gender: OneRosterGenderEnum?=null,
       val dateOfBirth: LocalDate?=null,
    )

    @Serializable
    sealed class RedeemInviteCredential

    @Serializable
    data class RedeemInvitePasswordCredential(val password: String) : RedeemInviteCredential()

    @Serializable
    data class RedeemInvitePasskeyCredential(
        val authResponseJson: AuthenticationResponseJSON
    ) : RedeemInviteCredential()

    @Serializable
    class Account(
        val username: String,
        val credential: RedeemInviteCredential,
    )

}