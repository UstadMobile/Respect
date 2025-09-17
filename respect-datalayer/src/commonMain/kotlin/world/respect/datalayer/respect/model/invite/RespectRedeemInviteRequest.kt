package world.respect.datalayer.respect.model.invite

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRoleEnum

@Serializable
class RespectRedeemInviteRequest(
    val code: String,
    val classUid: String?,
    val accountPersonInfo: PersonInfo,
    val role: PersonRoleEnum,
    val studentPersonInfo: PersonInfo?,
    val parentOrGuardianRole: GuardianRole?,
    val account: Account,
) {

    enum class GuardianRole {
        FATHER, MOTHER, OTHER_GUARDIAN
    }

    @Serializable
   data class PersonInfo(
        val name: String = "",
        val gender: PersonGenderEnum = PersonGenderEnum.UNSPECIFIED,
        val dateOfBirth: LocalDate,
    )

    @Serializable
    class Account(
        val username: String,
        val credential: String,//can be password or passkey
    )

}
