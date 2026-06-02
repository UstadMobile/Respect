package world.respect.shared.domain.launchapp

import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_BROWSABLE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import io.github.aakira.napier.Napier
import io.ktor.http.URLBuilder
import world.respect.shared.domain.launchapp.LaunchAppUseCase.Companion.RESPECT_LAUNCH_VERSION_PARAM_NAME
import world.respect.shared.domain.launchapp.LaunchAppUseCase.Companion.RESPECT_LAUNCH_VERSION_VALUE
import world.respect.shared.domain.launchapp.LaunchAppUseCase.LaunchRequest
import world.respect.shared.domain.xapi.getxapilaunchurl.GetXapiLaunchUrlUseCase

/**
 * Implementation of LaunchAppUseCase for Android.
 */
class LaunchAppUseCaseAndroid(
    private val appContext: Context,
    private val getXapiLaunchUrlUseCase: GetXapiLaunchUrlUseCase,
): LaunchAppUseCase {

    /**
     * Launch a compatible app for the given LaunchRequest: if a native app with verified app links
     * for the requested URL is installed, launch it. Otherwise, launch the requested URL in the
     * WebView Activity (where it is possible to intercept/cache resources etc).
     *
     * On Android SDK30 and above:
     *  Attempts to launch the url using an intent with FLAG_ACTIVITY_REQUIRE_NON_BROWSER as per
     *  https://developer.android.com/training/package-visibility/use-cases#let-non-browser-apps-handle-urls
     *
     * On Pre-SDK30 devices:
     *  Use the package manager to look for installed apps that can handle the intent (Pre-SDK30
     *  restrictions on querying packages are not enforced, so this technique works).
     */
    override suspend fun invoke(
        request: LaunchRequest
    ) {
        val launchUrlBase = getXapiLaunchUrlUseCase(
            publication = request.publication,
            publicationUrl = request.publicationUrl,
            assignmentActivityId = request.assignmentActivityId,
        )

        val launchUrl = URLBuilder(launchUrlBase).apply {
            parameters.append(
                RESPECT_LAUNCH_VERSION_PARAM_NAME, RESPECT_LAUNCH_VERSION_VALUE
            )
        }.build()

        try {
            val intent = Intent(
                Intent.ACTION_VIEW, launchUrl.toString().toUri()
            ).apply {
                addCategory(CATEGORY_BROWSABLE)
            }

            if(Build.VERSION.SDK_INT >= 30) {
                intent.flags = FLAG_ACTIVITY_REQUIRE_NON_BROWSER or FLAG_ACTIVITY_NEW_TASK
                Napier.d(
                    "LaunchAppUseCaseAndroid: attempting to launch $launchUrl with RequireNonBrowser"
                )
                appContext.startActivity(intent)
                return
            }else {
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                val resolvedInfo = appContext.packageManager.queryIntentActivities(
                    intent, PackageManager.MATCH_ALL
                )

                val availableNativePackages = resolvedInfo.mapNotNull {
                    it.activityInfo.packageName
                }.filterNot { it in KNOWN_BROWSER_PACKAGES }

                Napier.d("LaunchAppUseCaseAndroid: available native packages: $availableNativePackages")

                if(availableNativePackages.isNotEmpty()) {
                    appContext.startActivity(intent)
                    return
                }
            }
        }catch(e: Throwable) {
            Napier.w("Something wrong opening learning unit through app, fallback", e)
        }

        Log.i("LaunchUseCase", "Launching URL: $launchUrl")

        /*
         * The ActivityClass, because it's UI, is contained within the respect-app-compose module,
         * and is referenced using reflection. Activity names are not obfuscated by R8, so this is
         * safe.
         */
        val intent = Intent(
            appContext,
            Class.forName(WEBVIEW_ACTIVITY_NAME)
        )
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        val launchUrlStr = launchUrl.toString()
        intent.putExtra(EXTRA_URL, launchUrlStr)
        Napier.i("LaunchAppUseCaseAndroid: launching $launchUrlStr")
        appContext.startActivity(intent)
    }

    companion object {

        private const val WEBVIEW_ACTIVITY_NAME = "world.respect.WebViewActivity"

        const val EXTRA_URL = "url"

        private val KNOWN_BROWSER_PACKAGES = listOf(
            "org.chromium.webview_shell",//WebView on emulator
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.sec.android.app.sbrowser", //Samsung browser
            "com.mi.globalbrowser",//Mi browser
        )

    }
}