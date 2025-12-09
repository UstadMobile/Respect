package world.respect.shared.domain.biometric

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class BiometricAuthProcessor(
    activity: AppCompatActivity,
    private val jobChannel: Channel<BiometricAuthJob>,
    private val processOnScope: CoroutineScope,
) {
    val biometricManager = BiometricAuthManager(activity)

    suspend fun receiveJobs() {
        for (job in jobChannel) {
            processOnScope.launch {
                try {
                    if (!biometricManager.canAuthenticate()) {
                        job.response.complete(
                            BiometricAuthUseCase.BiometricResult.Error(-1, "not available")
                        )
                        return@launch
                    }

                    val promptData = BiometricAuthUseCase.BiometricPromptData(
                        title = job.request.title,
                        subtitle = job.request.subtitle,
                        description = job.request.description,
                        useDeviceCredential = job.request.useDeviceCredential,
                        negativeButtonText = job.request.negativeButtonText
                    )

                    val res: BiometricAuthUseCase.BiometricResult =
                        biometricManager.authenticate(promptData)

                    when (res) {
                        is BiometricAuthUseCase.BiometricResult.Success -> job.response.complete(
                            BiometricAuthUseCase.BiometricResult.Success
                        )

                        is BiometricAuthUseCase.BiometricResult.Failure -> job.response.complete(
                            BiometricAuthUseCase.BiometricResult.Failure(res.reason)
                        )

                        is BiometricAuthUseCase.BiometricResult.Canceled -> job.response.complete(
                            BiometricAuthUseCase.BiometricResult.Canceled
                        )

                        is BiometricAuthUseCase.BiometricResult.Error -> job.response.complete(
                            BiometricAuthUseCase.BiometricResult.Error(res.code, res.message)
                        )
                    }
                } catch (t: Throwable) {
                    job.response.complete(
                        BiometricAuthUseCase.BiometricResult.Error(
                            -99,
                            t.message ?: ""
                        )
                    )
                }
            }
        }
    }
}

