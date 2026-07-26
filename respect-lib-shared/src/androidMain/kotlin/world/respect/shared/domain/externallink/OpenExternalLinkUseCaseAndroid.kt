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
                Class.forName(WEB_VIEW_ACTIVITY_CLASS_NAME)
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(LaunchAppUseCaseAndroid.EXTRA_URL, url)
            if (title != null) {
                intent.putExtra(EXTRA_TITLE, title)
            }
            appContext.startActivity(intent)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to open external link: $url", e)
        }
    }

    companion object {
        private const val WEB_VIEW_ACTIVITY_CLASS_NAME = "world.respect.WebViewActivity"
        private const val EXTRA_TITLE = "title"
    }
}

