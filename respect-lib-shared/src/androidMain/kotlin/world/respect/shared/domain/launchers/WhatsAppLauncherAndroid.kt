package world.respect.shared.domain.launchers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhatsAppLauncherAndroid(
    private val context: Context
) : WhatsAppLauncher {

    // once whatsapp support number is available will change the default used
    override suspend fun launchWhatsApp() {
        withContext(Dispatchers.Main) {
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://wa.me/+919828932811")
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
