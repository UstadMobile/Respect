package world.respect.shared.domain.launchers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackViewModel.Companion.WEB_URL

class WebLauncherUseCaseAndroid(
    private val context: Context
) : WebLauncherUseCase {

    override suspend fun launchWeb() {
        withContext(Dispatchers.Main) {
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    WEB_URL.toUri()
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)

            } catch (e: ActivityNotFoundException) {
               print("No browser found to open web URL $e")
            }
        }
    }
}
