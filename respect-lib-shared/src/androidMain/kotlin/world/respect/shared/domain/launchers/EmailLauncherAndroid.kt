package world.respect.shared.domain.launchers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmailLauncherAndroid(
    private val context: Context
) : EmailLauncher {
    override suspend fun sendEmail() {
        withContext(Dispatchers.Main) {
            val uri = buildMailToUri()
            val intent = Intent(Intent.ACTION_SENDTO, uri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.w("EmailLinkLauncher", "No email app installed")
                throw e
            }
        }
    }
    private fun buildMailToUri(): Uri {
        val recipient = "manvi2346verma@gmail.com"
        val subject ="Feedback about RESPECT"
        return Uri.Builder()
            .scheme("mailto")
            .opaquePart(recipient)
            .appendQueryParameter("subject", subject)
            .build()
    }
}