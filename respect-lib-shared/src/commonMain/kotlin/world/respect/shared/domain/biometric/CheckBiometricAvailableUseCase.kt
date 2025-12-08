package world.respect.shared.domain.biometric

class CheckBiometricAvailableUseCase(private val biometricAuth: BiometricAuth) {
    operator fun invoke(): Boolean = biometricAuth.isAvailable()
}

class AuthenticateUseCase(private val biometricAuth: BiometricAuth) {
    suspend operator fun invoke(promptData: BiometricPromptData): BiometricResult =
        biometricAuth.authenticate(promptData)
}
