package world.respect.shared.domain.launchapp

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.shared.domain.launchapp.LaunchAppUseCase.Companion.RESPECT_LAUNCH_VERSION_PARAM_NAME
import world.respect.shared.domain.launchapp.LaunchAppUseCase.Companion.RESPECT_LAUNCH_VERSION_VALUE
import world.respect.shared.navigation.NavCommand

/**
 * Implementation of LaunchAppUseCase for Android.
 */
class LaunchAppUseCaseAndroid(
    private val appContext: Context,
): LaunchAppUseCase {

    override fun invoke(
        app: RespectAppManifest,
        learningUnitId: Url?,
        navigateFn: (NavCommand) -> Unit
    ) {
        val androidPackageId = app.android?.packageId
        val launchUrlBase = learningUnitId ?: Url(app.defaultLaunchUri.toString())
        val launchUrl = URLBuilder(launchUrlBase).apply {
            parameters.append(
                RESPECT_LAUNCH_VERSION_PARAM_NAME, RESPECT_LAUNCH_VERSION_VALUE
            )
        }.build()

        Log.i("LaunchUseCase", "Launching URL: $launchUrl")

        if(androidPackageId != null) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage(androidPackageId)
            intent.setData(launchUrl.toString().toUri())

            try {
                appContext.startActivity(intent)
                return
            }catch(e: Throwable) {
                e.printStackTrace()
                //In future - apps will need an action we can query to check if it is installed.
                //didn't work - maybe wasn't installed.
            }
        }

        /*
         * The ActivityClass, because it's UI, is contained within the respect-app-compose module,
         * and is referenced using reflection. Activity names are not obfuscated by R8, so this is
         * safe.
         */
        val intent = Intent(
            appContext,
            Class.forName(WEBVIEW_ACTIVITY_NAME)
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(EXTRA_URL, launchUrl.toString())
        appContext.startActivity(intent)
    }


    companion object {

        private const val WEBVIEW_ACTIVITY_NAME = "world.respect.WebViewActivity"

        const val EXTRA_URL = "url"

    }
}