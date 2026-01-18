package world.respect.shared.util

import io.ktor.http.Url
import world.respect.credentials.passkey.RespectQRBadgeCredential

object UrlParser {
    fun extractBadgeNumberFromUrl(url: Url): String? {
        val urlStr = url.toString()
        val idSegment = "/id/"
        if (!urlStr.contains(idSegment)) return null
        val afterId = urlStr.substringAfter(idSegment)
        val badgeId = afterId
            .substringBefore('?')
            .substringBefore('#')
            .substringBefore('/')

        return badgeId.takeIf { it.isNotEmpty() && it.all(Char::isDigit) }
    }

    /**
     * Extracts school base URL from QR badge URL.
     * Example: "http://192.168.20.15:8098/respect_qr_badge/id/123125" → "http://192.168.20.15:8098/"
     */
    fun extractSchoolBaseUrl(url: String): String? {
        return if (url.contains("/respect_qr_badge/id/")) {
            url.substringBefore("/respect_qr_badge/id/") + "/"
        } else {
            null
        }
    }
}

/**
 * Extension function for RespectQRBadgeCredential as suggested by Mike Dawson
 */
fun RespectQRBadgeCredential.extractSchoolUrl(): Url? {
    val baseUrl = UrlParser.extractSchoolBaseUrl(qrCodeUrl.toString())
    return baseUrl?.let { Url(it) }
}