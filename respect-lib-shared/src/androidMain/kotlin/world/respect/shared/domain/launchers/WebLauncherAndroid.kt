package world.respect.shared.domain.launchers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WebLauncherAndroid(
    private val context: Context
) : WebLauncher {

    override suspend fun launchWeb() {
        withContext(Dispatchers.Main) {
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://respect.world/")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.e("WebLauncherAndroid", "No browser found to open web URL", e)
            }
        }
    }
}
