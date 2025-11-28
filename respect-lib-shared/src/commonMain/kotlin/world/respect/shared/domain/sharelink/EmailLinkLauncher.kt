package world.respect.shared.domain.sharelink

interface EmailLinkLauncher {
    suspend fun sendEmail(subject: String, body: String)
}