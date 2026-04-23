package world.respect.shared.domain.account.invite

interface RedeemInviteExistingUserUseCase {

    suspend operator fun invoke(
        redeemRequest: RespectRedeemInviteRequest,
        selectedChildGuid : String ?=null
    )

}