package world.respect.shared.domain.sendinvite

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.domain.sharelink.SmsLinkLauncher
import world.respect.shared.domain.sharelink.SmsLinkLauncher.Companion.EXTRA_SMS_BODY
import world.respect.shared.domain.sharelink.SmsLinkLauncher.Companion.SMS_URI_SCHEME

class SmsLinkLauncherAndroid(private val context: Context) : SmsLinkLauncher {
    override suspend fun sendLink(body: String) = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, SMS_URI_SCHEME.toUri()).apply {
                putExtra(EXTRA_SMS_BODY, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Throwable) {
        }
    }
}
