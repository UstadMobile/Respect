package world.respect.shared.domain.school

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

class LaunchCustomTabUseCaseAndroid(
    private val appContext: Context
) : LaunchCustomTabUseCase {
    override fun invoke(url: String) {
        println("MAESTRO_DEBUG: onSelectDirectory called with LaunchCustomTabUseCaseAndroid : $url")
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .setUrlBarHidingEnabled(true)
                .build()
            customTabsIntent.intent.setPackage("com.android.chrome")
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            customTabsIntent.launchUrl(appContext, url.toUri())
            println("MAESTRO_DEBUG: onSelectDirectory called with LaunchCustomTabUseCaseAndroid try block")
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
            println("MAESTRO_DEBUG: onSelectDirectory called with LaunchCustomTabUseCaseAndroid: $e")
        }
    }
}
