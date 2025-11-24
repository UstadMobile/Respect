package world.respect.shared.domain.sharelink

interface SmsLinkLauncher {
    suspend fun sendLink(body: String)

    companion object {
        const val SMS_URI_SCHEME = "smsto:"
        const val EXTRA_SMS_BODY = "sms_body"
    }
}