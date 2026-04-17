package world.respect.shared.domain.externallink

import android.content.Context
import android.content.Intent
import world.respect.shared.domain.launchapp.LaunchAppUseCaseAndroid

class OpenExternalLinkUseCaseAndroid(
    private val appContext: Context,
) : OpenExternalLinkUseCase {
    
    override fun invoke(
        url: String,
        title: String?,
    ) {
        try {
            val intent = Intent(
                appContext,
                Class.forName("world.respect.WebViewActivity")
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(LaunchAppUseCaseAndroid.EXTRA_URL, url)
            if (title != null) {
                intent.putExtra("title", title)
            }
            appContext.startActivity(intent)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to open external link: $url", e)
        }
    }
}

