package world.respect.shared.domain.sharelink

interface LaunchSendEmailUseCase {
    suspend operator fun invoke(subject: String, body: String)
}