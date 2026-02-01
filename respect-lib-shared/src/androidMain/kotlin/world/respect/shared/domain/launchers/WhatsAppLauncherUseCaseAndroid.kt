package world.respect.shared.domain.launchers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackViewModel.Companion.WHATSAPP_URL
import androidx.core.net.toUri

class WhatsAppLauncherUseCaseAndroid(
    private val context: Context
) : WhatsAppLauncherUseCase {

    override suspend fun launchWhatsApp(respectPhoneNumber: String) {
        withContext(Dispatchers.Main) {
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "$WHATSAPP_URL$respectPhoneNumber".toUri()
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)

            } catch (e: ActivityNotFoundException) {
                print("WhatsApp not installed + $e")
            }
        }
    }
}
