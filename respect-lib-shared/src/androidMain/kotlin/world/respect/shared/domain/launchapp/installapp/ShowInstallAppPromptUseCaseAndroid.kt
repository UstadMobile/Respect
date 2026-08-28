package world.respect.shared.domain.launchapp.installapp

import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_BROWSABLE
import android.util.Log
import androidx.core.net.toUri
import io.ktor.http.URLBuilder
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import world.respect.lib.opds.model.findAppStoreAndroidLinks
import world.respect.shared.domain.activitycontextjobprocessor.SubmitActivityContextJobUseCase

/**
 *
 * Which will only work when:
 * https://developer.android.com/quality/core-value/app-eligibility
 */
class ShowInstallAppPromptUseCaseAndroid(
    private val submitActivityContextJobUseCase: SubmitActivityContextJobUseCase,
    private val appContext: Context,
): ShowInstallAppPromptUseCase {

    override suspend fun invoke(request: ShowInstallAppPromptUseCase.Request) {
        val appStoreLinks = request.launchableApp.findAppStoreAndroidLinks()

        val googlePlayLink = appStoreLinks.firstOrNull {
            it.href.startsWith("https://play.google.com/")
        }

        if(googlePlayLink != null) {
            val packageId = googlePlayLink.href.toUri().getQueryParameter("id")

            /*
             * As per
             * https://developer.android.com/distribute/marketing-tools/inline-installs
             *
             * Probably won't work because:
             * https://developer.android.com/quality/core-value/app-eligibility
             */
            val intent = Intent(Intent.ACTION_VIEW).also {
                it.data = "https://play.google.com/d?id=$packageId&referrer=${UrlEncoderUtil.encode(request.referrer)}".toUri()
                it.setPackage("com.android.vending")
                it.putExtra("overlay", true)
                it.putExtra("callerId", appContext.packageName)
            }

            if(appContext.packageManager.resolveActivity(intent, 0) != null) {
                submitActivityContextJobUseCase(
                    request = { activity ->
                        activity.startActivityForResult(intent, 0)
                    }
                )
                Log.i("ShowInstallAppPrompt", "Submitted activity job to launch ${intent.data} (maybe in overlay mode)")
                return
            }
        }

        if(appStoreLinks.isNotEmpty()){
            val intent = Intent(Intent.ACTION_VIEW).also {
                it.data = URLBuilder(appStoreLinks.first().href).apply {
                    encodedParameters["referrer"] = UrlEncoderUtil.encode(request.referrer)
                }.build().toString().toUri()
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                it.addCategory(CATEGORY_BROWSABLE)
            }

            appContext.startActivity(intent)
            Log.i("ShowInstallAppPrompt", "Submitted activity job to launch ${intent.data}")
        }else {
            Log.i("ShowInstallAppPrompt", "No app store links found for ${request.launchableApp.metadata.title}")
        }
    }
}