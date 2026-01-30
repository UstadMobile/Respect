package world.respect.shared.domain.feedback

fun interface GetFeedbackInfoUseCase {

    data class FeedBackInfo(
        val zammadToken:String,
        val zammadUrl:String,
        val respectPhoneNumber:String,
        val respectEmailId:String
    )

    suspend operator fun invoke(): FeedBackInfo
}