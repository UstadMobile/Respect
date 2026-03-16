package world.respect.shared.domain.feedback

import world.respect.shared.BuildConfig

class GetFeedbackInfoUseCaseImpl(): GetFeedbackInfoUseCase {
    override suspend fun invoke(): GetFeedbackInfoUseCase.FeedBackInfo {
        return GetFeedbackInfoUseCase.FeedBackInfo(
            zammadToken = BuildConfig.FEEDBACK_ZAMMADTOKEN,
            zammadUrl = BuildConfig.FEEDBACK_ZAMMADURL,
            respectPhoneNumber = BuildConfig.FEEDBACK_RESPECTPHONENUMBER,
            respectEmailId = BuildConfig.FEEDBACK_RESPECTEMAILID
        )
    }
}