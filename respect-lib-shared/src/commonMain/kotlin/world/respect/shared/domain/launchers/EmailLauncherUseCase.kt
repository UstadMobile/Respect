package world.respect.shared.domain.launchers

interface EmailLauncherUseCase {
    suspend fun sendEmail(subject: String)
}