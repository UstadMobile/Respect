package world.respect.shared.domain.launchers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackViewModel.Companion.WHATSAPP_URL
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackViewModel.Companion.WHATSAPP_PHONE_NUMBER
import androidx.core.net.toUri

class WhatsAppLauncherAndroid(
    private val context: Context
) : WhatsAppLauncher {

    // once whatsapp support number is available will change the default used
    override suspend fun launchWhatsApp() {
        withContext(Dispatchers.Main) {
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "$WHATSAPP_URL$WHATSAPP_PHONE_NUMBER".toUri()
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)

            } catch (e: ActivityNotFoundException) {
                Log.w("WhatsAppLauncher", "WhatsApp not installed", e)
            }
        }
    }


}
