package world.respect.shared.domain.launchers

interface EmailLauncher {
    suspend fun sendEmail()
}