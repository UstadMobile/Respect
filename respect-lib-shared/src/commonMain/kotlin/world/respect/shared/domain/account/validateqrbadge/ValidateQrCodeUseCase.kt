package world.respect.shared.domain.account.validateqrbadge

import io.ktor.http.Url
import world.respect.datalayer.SchoolDataSource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.qr_code_invalid_format
import world.respect.shared.generated.resources.qr_code_must_contain_respect_qr_badge
import world.respect.shared.generated.resources.qr_code_must_start_with_school_url
import world.respect.shared.util.exception.withUiText
import world.respect.shared.util.ext.asUiText

class ValidateQrCodeUseCase {

    /**
     * Validates a QR code URL for assignment to a student
     *
     * @param qrCodeUrl The QR code URL to validate
     * @param schoolUrl The school URL for validation
     * @param personGuid The person GUID to check assignments for
     * @param allowReplacement Whether to allow replacing existing QR code for same person
     */
    operator fun invoke(
        qrCodeUrl: String,
        schoolUrl: String?,
        personGuid: String? = null,
        allowReplacement: Boolean = false
    ) {
        // 1. Validate URL format
        validateUrlFormat(qrCodeUrl)

        // 2. Validate starts with school URL
        validateSchoolUrl(qrCodeUrl, schoolUrl)

        // 3. Validate contains /respect_qr_badge section
        validateQrBadgeSection(qrCodeUrl)

    }

    private fun validateUrlFormat(qrCodeUrl: String) {
        try {
            Url(qrCodeUrl)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid QR code URL format").withUiText(
                Res.string.qr_code_invalid_format.asUiText()
            )
        }
    }

    private fun validateSchoolUrl(qrCodeUrl: String, schoolUrl: String?) {
        if (schoolUrl != null && !qrCodeUrl.startsWith(schoolUrl)) {
            throw IllegalArgumentException("QR code must start with school URL").withUiText(
                Res.string.qr_code_must_start_with_school_url.asUiText()
            )
        }
    }

    private fun validateQrBadgeSection(qrCodeUrl: String) {
        if (!qrCodeUrl.contains("/respect_qr_badge", ignoreCase = true)) {
            throw IllegalArgumentException("QR code must contain /respect_qr_badge section").withUiText(
                Res.string.qr_code_must_contain_respect_qr_badge.asUiText()
            )
        }
    }

    /**
     * Quick validation without database checks (for UI validation)
     */
    fun validateFormatOnly(qrCodeUrl: String, schoolUrl: String?): Boolean {
        return try {
            Url(qrCodeUrl)

            if (schoolUrl != null && !qrCodeUrl.startsWith(schoolUrl)) {
                return false
            }

            qrCodeUrl.contains("/respect_qr_badge", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
}