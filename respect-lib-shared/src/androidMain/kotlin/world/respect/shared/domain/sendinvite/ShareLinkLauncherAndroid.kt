package world.respect.shared.domain.sendinvite

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.domain.sharelink.ShareLinkLauncher
import world.respect.shared.domain.sharelink.ShareLinkLauncher.Companion.MIME_TYPE

class ShareLinkLauncherAndroid(
    private val context: Context
) : ShareLinkLauncher {


    override suspend fun launch(body: String) = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE
                putExtra(Intent.EXTRA_TEXT, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val chooser = Intent.createChooser(intent, null).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(chooser)
        } catch (_: Throwable) { }
    }
}
