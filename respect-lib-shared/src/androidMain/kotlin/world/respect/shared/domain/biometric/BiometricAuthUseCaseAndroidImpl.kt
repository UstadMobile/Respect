package world.respect.shared.domain.biometric

class BiometricAuthUseCaseAndroidImpl(
    private val sender: BiometricAuthUseCaseAndroidChannelHost
) : BiometricAuthUseCase {

    override suspend fun invoke(request: BiometricAuthUseCase.BiometricPromptData): BiometricAuthUseCase.BiometricResult {
        val job = BiometricAuthJob(request)
        sender.requestChannel.send(job)
        return job.response.await()
    }
}
