package world.respect

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.ktor.http.Url
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope
import world.respect.app.domain.testing.SendDbToServerActivity
import world.respect.credentials.passkey.CreatePasskeyUseCaseAndroidChannelHost
import world.respect.credentials.passkey.CreatePasskeyUseCaseProcessor
import world.respect.credentials.passkey.GetCredentialUseCase
import world.respect.credentials.passkey.GetCredentialUseCaseAndroidImpl
import world.respect.credentials.passkey.GetCredentialUseCaseProcessor
import world.respect.credentials.passkey.password.SavePasswordUseCase
import world.respect.credentials.password.SavePasswordUseCaseAndroidImpl
import world.respect.credentials.password.SavePasswordUseCaseProcessor
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.libutil.ext.schoolUrlOrNull
import world.respect.shared.domain.biometric.BiometricAuthProcessor
import world.respect.shared.domain.biometric.BiometricAuthUseCaseAndroidImpl
import world.respect.shared.domain.navigation.deeplink.InitDeepLinkUriProviderUseCaseAndroid
import world.respect.shared.navigation.SendDbToServer
import world.respect.view.app.AbstractAppActivity

class MainActivity : AbstractAppActivity(), AndroidScopeComponent {

    //As per https://insert-koin.io/docs/reference/koin-android/scope/
    override val scope: Scope by activityScope()

    override fun onNewIntent(intent: Intent) {
        val launchUrl = intent.getStringExtra(InitDeepLinkUriProviderUseCaseAndroid.BUNDLE_ARG_NAME)
            ?: intent.data?.toString()
        if (launchUrl != null && redirectSendDbIfNeeded(launchUrl)) return
        super.onNewIntent(intent)
    }

    private fun redirectSendDbIfNeeded(launchUrl: String): Boolean {
        if (!launchUrl.contains(SendDbToServer.DEEP_LINK_PATH)) return false
        val url = Url(launchUrl)
        val schoolUrl = url.schoolUrlOrNull() ?: return false
        val name = url.parameters[SendDbToServer.QUERY_PARAM_NAME] ?: return false
        startActivity(SendDbToServerActivity.createIntent(this, schoolUrl.toString(), name))
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val launchUrl = intent.getStringExtra(InitDeepLinkUriProviderUseCaseAndroid.BUNDLE_ARG_NAME)
            ?: intent.data?.toString()
        if (launchUrl != null && redirectSendDbIfNeeded(launchUrl)) {
            finish()
            return
        }

        checkNotNull(scope)

        val koin = getKoin()

        val createPasskeyChannelHost = koin.get<CreatePasskeyUseCaseAndroidChannelHost>()
        val getCredentialUseCase = koin.get<GetCredentialUseCase>()
                as GetCredentialUseCaseAndroidImpl
        val savePasswordUseCase = koin.get<SavePasswordUseCase>()
                as SavePasswordUseCaseAndroidImpl
        val biometricUseCase = koin.get<BiometricAuthUseCaseAndroidImpl>()

        val createPasskeyProcessor = CreatePasskeyUseCaseProcessor(
            activityContext = this,
            jobChannel = createPasskeyChannelHost.requestChannel,
            processOnScope = lifecycleScope
        )

        val getCredentialProcessor = GetCredentialUseCaseProcessor(
            activityContext = this,
            channel = getCredentialUseCase.requestChannel,
            processOnScope = lifecycleScope
        )

        val savePasswordProcessor = SavePasswordUseCaseProcessor(
            activityContext = this,
            jobChannel = savePasswordUseCase.requestChannel,
            processOnScope = lifecycleScope
        )

        val biometricProcessor = BiometricAuthProcessor(
            activity = this,
            jobChannel = biometricUseCase.requestChannel,
            processOnScope = lifecycleScope
        )

        //Launch processors for jobs that need an activity context.
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    createPasskeyProcessor.receiveJobs()
                }
                launch {
                    getCredentialProcessor.receiveJobs()
                }
                launch {
                    savePasswordProcessor.receiveJobs()
                }
                launch {
                    biometricProcessor.receiveJobs()
                }
            }
        }

        /*
         * Set a specific school directory to use based on bundle arguments (normally, but not
         * necessarily, for end-to-end testing purposes).
         */
        intent.extras?.getString(EXTRA_RESPECT_DIRECTORY)?.also { directoryUrl ->
            lifecycleScope.launch {
                val respectAppDataSource = getKoin().get<RespectAppDataSource>()
                respectAppDataSource.schoolDirectoryDataSource.insertOrIgnore(
                    schoolDirectory = RespectSchoolDirectory(
                        invitePrefix = "",
                        baseUrl = Url(directoryUrl)
                    ),
                    clearOthers = true,
                )
            }
        }
    }

    companion object {

        /**
         *
         */
        const val EXTRA_RESPECT_DIRECTORY = "respect_directory"

    }
}
