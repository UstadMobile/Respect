package world.respect.shared.domain.launchers

interface WhatsAppLauncherUseCase {
    suspend fun launchWhatsApp(respectPhoneNumber: String)
}

