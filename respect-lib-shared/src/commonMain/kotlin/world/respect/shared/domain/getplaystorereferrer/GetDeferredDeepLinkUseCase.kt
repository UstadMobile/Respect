package world.respect.shared.domain.getplaystorereferrer

/**
 * Get a deferred deep link: If a user opens a link that was shared with them, but they don't have
 * the app installed, we will try to use a referrerUrl that can then be retrieved when the
 * installation is complete such that they can continue seamlessly when they first open the app.
 *
 * On Android this is done using the Play Install Referrer Library:
 * https://developer.android.com/google/play/installreferrer/library
 */
interface GetDeferredDeepLinkUseCase {

    suspend operator fun invoke(): String?

    companion object {

        const val PARAM_NAME_DEFERRED_DEEP_LINK = "deferredDeepLink"

    }


}