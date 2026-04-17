package world.respect.shared.domain.externallink

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

interface ExtractWebPageMetadataUseCase {
    suspend operator fun invoke(url: String): WebPageMetadata
}

data class WebPageMetadata(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
)


class ExtractWebPageMetadataUseCaseImpl(
    private val httpClient: HttpClient,
) : ExtractWebPageMetadataUseCase {

    override suspend fun invoke(url: String): WebPageMetadata {
        return try {
            val html = httpClient.get(url).bodyAsText()
            val imageUrl = extractMetaImage(html)
            val resolvedImageUrl = imageUrl?.let { resolveUrl(it, url) }
            WebPageMetadata(
                title = extractTitle(html),
                description = extractMetaDescription(html),
                imageUrl = resolvedImageUrl,
            )
        } catch (_: Exception) {
            WebPageMetadata()
        }
    }
    private fun extractTitle(html: String): String? {
        val startIndex = html.indexOf("<title", ignoreCase = true)
        if (startIndex == -1) return null
        val contentStart = html.indexOf(">", startIndex) + 1
        val endIndex = html.indexOf("</title>", contentStart, ignoreCase = true)
        if (endIndex <= contentStart) return null
        return html.substring(contentStart, endIndex).trim().takeIf { it.isNotBlank() }
    }
    private fun extractMetaDescription(html: String): String? {
        val lowerHtml = html.lowercase()
        var searchStart = 0
        while (true) {
            val metaIdx = lowerHtml.indexOf("<meta", searchStart)
            if (metaIdx == -1) return null
            val tagEnd = lowerHtml.indexOf(">", metaIdx)
            if (tagEnd == -1) return null
            val tagContent = lowerHtml.substring(metaIdx, tagEnd + 1)
            if (tagContent.contains("name=\"description\"")) {
                val originalTag = html.substring(metaIdx, tagEnd + 1)
                val contentIdx = originalTag.lowercase().indexOf("content=\"")
                if (contentIdx != -1) {
                    val valueStart = contentIdx + "content=\"".length
                    val valueEnd = originalTag.indexOf("\"", valueStart)
                    if (valueEnd > valueStart) {
                        return originalTag.substring(valueStart, valueEnd).trim()
                    }
                }
            }
            searchStart = tagEnd + 1
        }
    }

    private fun extractMetaImage(html: String): String? {
        val lowerHtml = html.lowercase()
        var searchStart = 0
        while (true) {
            val metaIdx = lowerHtml.indexOf("<meta", searchStart)
            if (metaIdx == -1) break
            val tagEnd = lowerHtml.indexOf(">", metaIdx)
            if (tagEnd == -1) break
            val tagContent = lowerHtml.substring(metaIdx, tagEnd + 1)
            if (tagContent.contains("property=\"og:image\"")) {
                val originalTag = html.substring(metaIdx, tagEnd + 1)
                val contentIdx = originalTag.lowercase().indexOf("content=\"")
                if (contentIdx != -1) {
                    val valueStart = contentIdx + "content=\"".length
                    val valueEnd = originalTag.indexOf("\"", valueStart)
                    if (valueEnd > valueStart) {
                        return originalTag.substring(valueStart, valueEnd).trim()
                    }
                }
            }
            searchStart = tagEnd + 1
        }
        searchStart = 0
        while (true) {
            val metaIdx = lowerHtml.indexOf("<meta", searchStart)
            if (metaIdx == -1) return null
            val tagEnd = lowerHtml.indexOf(">", metaIdx)
            if (tagEnd == -1) return null
            val tagContent = lowerHtml.substring(metaIdx, tagEnd + 1)
            if (tagContent.contains("name=\"image\"")) {
                val originalTag = html.substring(metaIdx, tagEnd + 1)
                val contentIdx = originalTag.lowercase().indexOf("content=\"")
                if (contentIdx != -1) {
                    val valueStart = contentIdx + "content=\"".length
                    val valueEnd = originalTag.indexOf("\"", valueStart)
                    if (valueEnd > valueStart) {
                        return originalTag.substring(valueStart, valueEnd).trim()
                    }
                }
            }
            searchStart = tagEnd + 1
        }
    }

    private fun resolveUrl(imageUrl: String, baseUrl: String): String {
        // If already absolute URL (starts with http:// or https://), return as is
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl
        }
        
        // Parse base URL to get the base path
        val protocolEnd = baseUrl.indexOf("://")
        if (protocolEnd == -1) return imageUrl
        
        val pathStart = baseUrl.indexOf("/", protocolEnd + 3)
        if (pathStart == -1) {
            // No path, just domain
            return baseUrl + "/" + imageUrl
        }
        
        // Get the directory of the base URL
        val lastSlash = baseUrl.lastIndexOf("/")
        val basePath = baseUrl.substring(0, lastSlash + 1)
        
        return basePath + imageUrl
    }
}




