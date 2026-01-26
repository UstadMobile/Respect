package world.respect.shared.domain.navigation.deeplink

import android.content.Intent
import android.net.Uri
import java.util.concurrent.atomic.AtomicReference

/**
 * Implementation of InitDeepLinkUriProviderUseCase where the Activity can call onSetDeepLink from
 * its onCreate function. The initial deep link can then be retrieved by the
 * AcknowledgementViewModel which can use it to direct the user to the appropriate deep link if
 * required.
 */
class InitDeepLinkUriProviderUseCaseAndroid(): InitDeepLinkUriProviderUseCase {

    private val initDeepLinkUri = AtomicReference<Uri?>(null)

    fun onSetDeepLink(
        intent: Intent
    ) {
        initDeepLinkUri.set(intent.data)
    }

    override fun invoke(): String? {
        return initDeepLinkUri.get()?.toString()
    }
}