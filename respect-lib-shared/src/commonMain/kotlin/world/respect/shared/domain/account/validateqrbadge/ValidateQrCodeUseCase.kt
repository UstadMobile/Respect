package world.respect.shared.domain.account.validateqrbadge

import io.ktor.http.Url
import org.jetbrains.compose.resources.StringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*

data class QrValidationResult(
    val isValid: Boolean = true,
    val errorMessage: StringResource? = null
) {
    companion object {
        val Valid = QrValidationResult(isValid = true)
        val InvalidFormat = QrValidationResult(
            isValid = false,
            errorMessage = Res.string.qr_code_invalid_format
        )
        val InvalidSchoolUrl = QrValidationResult(
            isValid = false,
            errorMessage = Res.string.qr_code_must_start_with_school_url
        )
        val MissingBadgeSection = QrValidationResult(
            isValid = false,
            errorMessage = Res.string.qr_code_must_contain_respect_qr_badge
        )
    }
}

class ValidateQrCodeUseCase(
    private val schoolUrl: Url?
) {

    operator fun invoke(
        qrCodeUrl: String,
        personGuid: String? = null,
    ): QrValidationResult {
        // 1. Validate URL format
        val formatValidation = validateUrlFormat(qrCodeUrl)
        if (!formatValidation.isValid) return formatValidation

        // 2. Validate starts with school URL
        val schoolUrlValidation = validateSchoolUrl(qrCodeUrl)
        if (!schoolUrlValidation.isValid) return schoolUrlValidation

        // 3. Validate contains /respect_qr_badge section
        val badgeValidation = validateQrBadgeSection(qrCodeUrl)
        if (!badgeValidation.isValid) return badgeValidation

        return QrValidationResult.Valid
    }

    private fun validateUrlFormat(qrCodeUrl: String): QrValidationResult {
        return try {
            Url(qrCodeUrl)
            QrValidationResult.Valid
        } catch (e: Exception) {
            QrValidationResult.InvalidFormat
        }
    }

    private fun validateSchoolUrl(qrCodeUrl: String): QrValidationResult {
        return if (schoolUrl != null && !qrCodeUrl.startsWith(schoolUrl.toString())) {
            QrValidationResult.InvalidSchoolUrl
        } else {
            QrValidationResult.Valid
        }
    }

    private fun validateQrBadgeSection(qrCodeUrl: String): QrValidationResult {
        return if (!qrCodeUrl.contains("/respect_qr_badge", ignoreCase = true)) {
            QrValidationResult.MissingBadgeSection
        } else {
            QrValidationResult.Valid
        }
    }

    /**
     * Quick validation without database checks (for UI validation)
     */
    fun validateFormatOnly(qrCodeUrl: String): QrValidationResult {
        val formatCheck = validateUrlFormat(qrCodeUrl)
        if (!formatCheck.isValid) return formatCheck

        val schoolCheck = validateSchoolUrl(qrCodeUrl)
        if (!schoolCheck.isValid) return schoolCheck

        return validateQrBadgeSection(qrCodeUrl)
    }
}