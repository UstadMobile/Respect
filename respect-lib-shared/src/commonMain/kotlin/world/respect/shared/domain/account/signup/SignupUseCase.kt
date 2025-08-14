package world.respect.shared.domain.account.signup

class SignupUseCase() {
    suspend operator fun invoke(credential: SignupCredential) {
        when (credential) {
            is SignupCredential.Password -> {
            }
            is SignupCredential.Passkey -> {

            }
        }
    }
}
