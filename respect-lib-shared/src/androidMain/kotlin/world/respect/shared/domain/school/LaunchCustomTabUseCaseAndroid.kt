package world.respect.shared.domain.school

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import io.github.aakira.napier.Napier

class LaunchCustomTabUseCaseAndroid(
    private val appContext: Context
) : LaunchCustomTabUseCase {

    override fun invoke(url: String) {
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .setUrlBarHidingEnabled(true)
                .build()
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            customTabsIntent.launchUrl(appContext, url.toUri())
            Napier.i("LaunchCustomTabUseCaseAndroid: Launched custom tab for $url")
        } catch (e: Exception) {
            Napier.e(throwable = e) { "LaunchCustomTabUseCaseAndroid: Error launching custom tab" }
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
        }
    }

}