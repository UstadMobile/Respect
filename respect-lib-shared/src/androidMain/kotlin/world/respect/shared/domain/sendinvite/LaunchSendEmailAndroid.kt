package world.respect.shared.domain.sendinvite

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.domain.sharelink.LaunchSendEmailUseCase

class LaunchSendEmailAndroid(
    private val context: Context
) : LaunchSendEmailUseCase {

    override suspend fun sendEmail(subject: String, body: String) {
        withContext(Dispatchers.Main) {
            val uri = buildMailToUri(subject, body)
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

    private fun buildMailToUri(subject: String, body: String): Uri {
        return Uri.Builder()
            .scheme("mailto")
            .appendQueryParameter("subject", subject)
            .appendQueryParameter("body", body)
            .build()
    }

}
