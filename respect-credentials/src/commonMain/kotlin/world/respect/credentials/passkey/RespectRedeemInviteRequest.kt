package world.respect.credentials.passkey

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRoleEnum

/**
 *
 */
@Serializable
class RespectRedeemInviteRequest(
    val code: String,
    val classUid: String?,
    val role: PersonRoleEnum,
    val accountPersonInfo: PersonInfo,
    val parentOrGuardianRole: GuardianRole?,
    val account: Account,
) {

    @Serializable
    enum class GuardianRole {
        FATHER, MOTHER, OTHER_GUARDIAN
    }

   @Serializable
   data class PersonInfo(
       val name: String = "",
       val gender: PersonGenderEnum = PersonGenderEnum.UNSPECIFIED,
       val dateOfBirth: LocalDate = LocalDate(1900, 1, 1),
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