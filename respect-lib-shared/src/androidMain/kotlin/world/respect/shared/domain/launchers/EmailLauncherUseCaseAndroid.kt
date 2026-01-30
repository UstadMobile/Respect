package world.respect.shared.domain.launchers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

class EmailLauncherUseCaseAndroid(
    private val context: Context
) : EmailLauncherUseCase {

    override suspend fun sendEmail(respectEmailId: String, subject: String) {
        withContext(Dispatchers.Main) {
            val uri = buildMailToUri(respectEmailId, subject)
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

    private fun buildMailToUri(respectEmailId: String, subject: String): Uri {
        val encodedSubject = Uri.encode(subject)
        return "mailto:$respectEmailId?subject=$encodedSubject".toUri()
    }
}

