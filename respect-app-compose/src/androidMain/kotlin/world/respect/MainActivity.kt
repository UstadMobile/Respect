package world.respect

import android.app.LocaleManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.ktor.http.Url
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope
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
import world.respect.shared.domain.biometric.BiometricAuthProcessor
import world.respect.shared.domain.biometric.BiometricAuthUseCaseAndroidImpl
import world.respect.view.app.AbstractAppActivity
import kotlin.jvm.java

class MainActivity : AbstractAppActivity(), AndroidScopeComponent {

    //As per https://insert-koin.io/docs/reference/koin-android/scope/
    override val scope: Scope by activityScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotNull(scope)

        val koin = getKoin()

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = getSystemService(LocaleManager::class.java)
            localeManager?.applicationLocales ?: LocaleListCompat.getEmptyLocaleList()
        } else {
            AppCompatDelegate.getApplicationLocales()
        }


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

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}