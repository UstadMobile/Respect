package world.respect

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException
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
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.activitycontextjobprocessor.ActivityContextJobProcessor
import world.respect.shared.domain.activitycontextjobprocessor.EnqueueActivityContextJobUseCase
import world.respect.shared.domain.biometric.BiometricAuthProcessor
import world.respect.shared.domain.biometric.BiometricAuthUseCaseAndroidImpl
import world.respect.shared.navigation.Home
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.WaitingForApproval
import world.respect.view.app.AbstractAppActivity

class MainActivity : AbstractAppActivity(), AndroidScopeComponent {

    //As per https://insert-koin.io/docs/reference/koin-android/scope/
    override val scope: Scope by activityScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotNull(scope)

        val koin = getKoin()

        val createPasskeyChannelHost = koin.get<CreatePasskeyUseCaseAndroidChannelHost>()
        val getCredentialUseCase = koin.get<GetCredentialUseCase>()
                as GetCredentialUseCaseAndroidImpl
        val biometricUseCase = koin.get<BiometricAuthUseCaseAndroidImpl>()
        val enqueueActivityContextJobUseCase = koin.get<EnqueueActivityContextJobUseCase>()

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

        val biometricProcessor = BiometricAuthProcessor(
            activity = this,
            jobChannel = biometricUseCase.requestChannel,
            processOnScope = lifecycleScope
        )

        val activityJobProcessor = ActivityContextJobProcessor(
            activityContext = this,
            jobChannel = enqueueActivityContextJobUseCase.jobChannel,
            processOnScope = lifecycleScope,
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
                    biometricProcessor.receiveJobs()
                }

                launch {
                    activityJobProcessor.receiveJobs()
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

        handleOpenIdAuthorizationResult(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOpenIdAuthorizationResult(intent)
    }

    private fun handleOpenIdAuthorizationResult(intent: Intent) {
        lifecycleScope.launch {
            try {
                val callbackResult = getKoin()
                    .get<HandleOpenIdAuthorizationResultUseCaseAndroid>()(intent = intent)
                if (callbackResult is HandleOpenIdAuthorizationResultUseCaseAndroid.Result.Authenticated) {
                    val accountManager = getKoin().get<RespectAccountManager>()
                    accountManager.startSession(
                        authResponse = callbackResult.authResponse,
                        schoolUrl = callbackResult.schoolUrl,
                    )

                    sendNavigationCommand(
                        NavCommand.Navigate(
                            destination = if (
                                callbackResult.authResponse.person.status ==
                                PersonStatusEnum.PENDING_APPROVAL
                            ) {
                                WaitingForApproval()
                            } else {
                                Home
                            },
                            clearBackStack = true,
                        )
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                Napier.e(
                    message = "Unable to complete OpenID sign-in",
                    throwable = exception,
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
