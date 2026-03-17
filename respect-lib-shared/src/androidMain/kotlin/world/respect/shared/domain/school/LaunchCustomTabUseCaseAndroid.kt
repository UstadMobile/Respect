package world.respect.shared.domain.school

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

class LaunchCustomTabUseCaseAndroid(
    private val appContext: Context
) : LaunchCustomTabUseCase {
    override fun invoke(url: String) {
        println("MAESTRO_DEBUG: onSelectDirectory called with LaunchCustomTabUseCaseAndroid : $url")
        val uri = url.toUri()
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .setUrlBarHidingEnabled(true)
                .build()
            
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // To prevent our own app from intercepting this if it's an App Link
            val browserPackage = getBrowserPackageName(appContext, uri)
            if (browserPackage != null && browserPackage != appContext.packageName) {
                customTabsIntent.intent.setPackage(browserPackage)
            }

            customTabsIntent.launchUrl(appContext, uri)
            println("MAESTRO_DEBUG: launchCustomTabUseCase completed successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
            println("MAESTRO_DEBUG: onSelectDirectory called with LaunchCustomTabUseCaseAndroid error: $e")
        }
    }

    private fun getBrowserPackageName(context: Context, uri: Uri): String? {
        val pm = context.packageManager
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        val resolveInfo = pm.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val packageName = resolveInfo?.activityInfo?.packageName
        
        // If the resolved package is our own, we try to find an actual browser
        if (packageName == context.packageName) {
            val handlers = pm.queryIntentActivities(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (handler in handlers) {
                if (handler.activityInfo.packageName != context.packageName) {
                    return handler.activityInfo.packageName
                }
            }
        }
        return packageName
    }
}
