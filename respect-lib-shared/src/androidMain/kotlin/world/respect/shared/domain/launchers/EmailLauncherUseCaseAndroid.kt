package world.respect.shared.domain.launchers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackViewModel.Companion.EMAIL_RECIPIENT

class EmailLauncherUseCaseAndroid(
    private val context: Context
) : EmailLauncherUseCase {

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
        val encodedSubject = Uri.encode(subject)
        return "mailto:$EMAIL_RECIPIENT?subject=$encodedSubject".toUri()
    }
}

