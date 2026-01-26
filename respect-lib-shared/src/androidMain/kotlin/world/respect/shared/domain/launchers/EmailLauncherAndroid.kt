package world.respect.shared.domain.launchers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.feedback_respect
import world.respect.shared.util.ext.asUiText

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
        val subject = Res.string.feedback_respect.asUiText().toString()
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

