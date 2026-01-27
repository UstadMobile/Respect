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
    override suspend fun sendEmail(subject: String) {
        withContext(Dispatchers.Main) {
            val uri = buildMailToUri(subject)
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
    private fun buildMailToUri(subject: String): Uri {
        val recipient = "manvi2346verma@gmail.com"
        return Uri.Builder()
            .scheme(MAIL_TO_URI)
            .opaquePart(recipient)
            .appendQueryParameter(MAIL_TO_SUBJECT, subject)
            .build()
    }

    companion object{
        const val MAIL_TO_URI = "mailto:"
        const val MAIL_TO_SUBJECT = "?subject="
    }
}

