package world.respect.shared.domain.sharelink

interface LaunchSendEmailUseCase {
    suspend fun sendEmail(subject: String, body: String)
}