package world.respect.domain.opds.validator

import world.respect.domain.opds.model.OpdsFeed
import world.respect.domain.opds.model.OpdsFeedMetadata
import world.respect.domain.opds.model.OpdsPublication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.SerializationException
import java.io.File
import java.net.URI
import java.net.URL

/**
 * Validates OPDS feeds and publications according to the OPDS 2.0 specification.
 * In RESPECT context, OPDS feeds are used to list learning units, where each
 * publication represents a distinct learning unit.
 * For reference, see the schemas:
 * - https://drafts.opds.io/schema/feed.schema.json
 * - https://drafts.opds.io/schema/publication.schema.json
 */
class OpdsValidator(
    private val json: Json = Json {
        ignoreUnknownKeys = false
        isLenient = false
        prettyPrint = true
    }
) {
    /**
     * Validates OPDS content from URI, supporting both local file paths and HTTP(S) URLs.
     * If followLinks is true, it will also validate all linked feeds.
     * @param uri The URI to the OPDS content (file:// or http(s)://)
     * @param followLinks Whether to follow links in the feed
     * @return A ValidationResult indicating success or failure
     */
    fun validateOpdsUri(uri: URI, followLinks: Boolean = true): ValidationResult {
        val warnings = mutableListOf<String>()
        try {
            // Add this new check right here, before fetching content
            if (uri.toString().contains("/schema/") && uri.toString().endsWith(".schema.json")) {
                return ValidationResult.Exception(
                    "The URI points to a JSON Schema file which defines the OPDS specification, not an actual OPDS feed.",
                    IllegalArgumentException("Please provide an OPDS feed or publication to validate, not the schema itself.")
                )
            }

            // Fetch content from URI
            val content = when (uri.scheme) {
                "file" -> {
                    File(uri.path).readText()
                }
                "http", "https" -> {
                    val connection = URL(uri.toString()).openConnection()
                    connection.connectTimeout = 10000 // 10 seconds
                    connection.readTimeout = 10000 // 10 seconds
                    connection.getInputStream().bufferedReader().use { it.readText() }
                }
                else -> {
                    return ValidationResult.Error(listOf("Unsupported URI scheme: ${uri.scheme}"))
                }
            }

            // Validate the content
            return validateOpdsContent(content, uri.toString(), followLinks, warnings)

        } catch (e: Exception) {
            return ValidationResult.Exception("Error accessing or reading URI: $uri", e)
        }
    }

    /**
     * Validates OPDS content string. If followLinks is true and baseUrl is provided,
     * it will also validate all linked feeds.
     * @param content The OPDS content as a string
     * @param baseUrl The base URL used for resolving relative links (optional)
     * @param followLinks Whether to follow links in the feed
     * @param warnings List to collect warning messages
     * @return A ValidationResult indicating success or failure
     */
    private fun validateOpdsContent(
        content: String,
        baseUrl: String = "",
        followLinks: Boolean = true,
        warnings: MutableList<String> = mutableListOf()
    ): ValidationResult {
        try {
            // First, check if it's valid JSON and analyze to determine type
            val jsonElement = json.parseToJsonElement(content)
            if (jsonElement !is JsonObject) {
                return ValidationResult.Error(listOf("Not a valid JSON object"))
            }

            // Check if the JSON object is empty
            if (jsonElement.isEmpty()) {
                return ValidationResult.Error(
                    listOf("Empty JSON object. OPDS feed or publication requires specific properties.")
                )
            }

            // Check for required fields for either feed or publication
            val hasMetadata = jsonElement.containsKey("metadata")
            val hasLinks = jsonElement.containsKey("links")

            if (!hasMetadata || !hasLinks) {
                return ValidationResult.Error(
                    listOf("Missing required fields. Both 'metadata' and 'links' are required for OPDS feeds and publications.")
                )
            }

            // Determine if this is more likely a feed or publication
            val isProbablyFeed = isProbablyFeed(jsonElement)

            // Try the most likely format first
            if (isProbablyFeed) {
                try {
                    val feed = json.decodeFromString<OpdsFeed>(content)
                    val result = validateFeed(feed, warnings)

                    if (result.isSuccess) {
                        // If followLinks is true and we have a baseUrl, follow links in the feed
                        if (followLinks && baseUrl.isNotEmpty()) {
                            // Store URLs we've already visited to prevent loops
                            val visitedUrls = mutableSetOf<String>()
                            visitedUrls.add(baseUrl)

                            // Track errors from linked feeds
                            val linkedErrors = mutableListOf<String>()

                            // Follow links in the feed
                            followLinks(feed, baseUrl, visitedUrls, linkedErrors, warnings, 1)

                            // If we found errors in linked feeds, include them in the result
                            if (linkedErrors.isNotEmpty()) {
                                return ValidationResult.Error(linkedErrors)
                            }
                        }

                        return ValidationResult.Success(FeedType.FEED, warnings.toList())
                    } else {
                        // Try as publication if feed validation failed
                        val feedErrors = result.exceptionOrNull()?.let {
                            (it as? ValidationException)?.errors ?:
                            listOf(it.message ?: "Unknown feed validation error")
                        } ?: listOf("Unknown feed validation error")

                        try {
                            val publication = json.decodeFromString<OpdsPublication>(content)
                            val pubResult = validatePublication(publication, warnings)

                            return if (pubResult.isSuccess) {
                                ValidationResult.Success(FeedType.PUBLICATION, warnings.toList())
                            } else {
                                // Both failed, show feed errors since that was more likely
                                ValidationResult.Error(feedErrors)
                            }
                        } catch (e: SerializationException) {
                            // Only feed validation was possible, show those errors
                            return ValidationResult.Error(feedErrors)
                        }
                    }
                } catch (e: SerializationException) {
                    // Try as publication instead
                    try {
                        val publication = json.decodeFromString<OpdsPublication>(content)
                        val result = validatePublication(publication, warnings)

                        return if (result.isSuccess) {
                            ValidationResult.Success(FeedType.PUBLICATION, warnings.toList())
                        } else {
                            val errors = result.exceptionOrNull()?.let {
                                (it as? ValidationException)?.errors ?:
                                listOf(it.message ?: "Unknown publication validation error")
                            } ?: listOf("Unknown publication validation error")
                            ValidationResult.Error(errors)
                        }
                    } catch (e2: SerializationException) {
                        return ValidationResult.Exception(
                            "The content is not a valid OPDS feed or publication",
                            e2
                        )
                    }
                }
            } else {
                // Try as publication first
                try {
                    val publication = json.decodeFromString<OpdsPublication>(content)
                    val result = validatePublication(publication, warnings)

                    if (result.isSuccess) {
                        return ValidationResult.Success(FeedType.PUBLICATION, warnings.toList())
                    } else {
                        // Only try as feed if publication validation failed
                        val pubErrors = result.exceptionOrNull()?.let {
                            (it as? ValidationException)?.errors ?:
                            listOf(it.message ?: "Unknown publication validation error")
                        } ?: listOf("Unknown publication validation error")

                        try {
                            val feed = json.decodeFromString<OpdsFeed>(content)
                            val feedResult = validateFeed(feed, warnings)

                            return if (feedResult.isSuccess) {
                                ValidationResult.Success(FeedType.FEED, warnings.toList())
                            } else {
                                // Both failed, show publication errors since that was more likely
                                ValidationResult.Error(pubErrors)
                            }
                        } catch (e: SerializationException) {
                            // Only publication validation was possible, show those errors
                            return ValidationResult.Error(pubErrors)
                        }
                    }
                } catch (e: SerializationException) {
                    // Try as feed instead
                    try {
                        val feed = json.decodeFromString<OpdsFeed>(content)
                        val result = validateFeed(feed, warnings)

                        return if (result.isSuccess) {
                            ValidationResult.Success(FeedType.FEED, warnings.toList())
                        } else {
                            val errors = result.exceptionOrNull()?.let {
                                (it as? ValidationException)?.errors ?:
                                listOf(it.message ?: "Unknown feed validation error")
                            } ?: listOf("Unknown feed validation error")
                            ValidationResult.Error(errors)
                        }
                    } catch (e2: SerializationException) {
                        return ValidationResult.Exception(
                            "The content is not a valid OPDS feed or publication",
                            e2
                        )
                    }
                }
            }
        } catch (e: Exception) {
            return ValidationResult.Exception("Invalid JSON structure in content", e)
        }
    }

    /**
     * Follows links in an OPDS feed recursively to validate the entire feed network.
     *
     * @param feed The feed to follow links from
     * @param baseUrl The base URL of the feed
     * @param visitedUrls Set of already visited URLs to prevent loops
     * @param errors List to collect validation errors
     * @param warnings List to collect warning messages
     * @param depth Current recursion depth
     */
    private fun followLinks(
        feed: OpdsFeed,
        baseUrl: String,
        visitedUrls: MutableSet<String>,
        errors: MutableList<String>,
        warnings: MutableList<String>,
        depth: Int
    ) {
        // Stop if we've reached the maximum depth
        if (depth > MAX_LINK_DEPTH) {
            warnings.add("Maximum link depth ($MAX_LINK_DEPTH) reached at $baseUrl")
            return
        }

        // Collect all links that point to OPDS feeds
        val links = mutableListOf<String>()

        // Add navigation links
//        feed.navigation?.forEach { link ->
//            if (link.type == OpdsFeed.MEDIA_TYPE || link.type == OpdsPublication.MEDIA_TYPE) {
//                links.add(link.href)
//            }
//        }
//
//        // Add other links with OPDS media types
//        feed.links.forEach { link ->
//            if (link.type == OpdsFeed.MEDIA_TYPE || link.type == OpdsPublication.MEDIA_TYPE) {
//                links.add(link.href)
//            }
//        }
//
//        // Add links from groups
//        feed.groups?.forEach { group ->
//            group.navigation?.forEach { link ->
//                if (link.type == OpdsFeed.MEDIA_TYPE || link.type == OpdsPublication.MEDIA_TYPE) {
//                    links.add(link.href)
//                }
//            }
//
//            group.links?.forEach { link ->
//                if (link.type == OpdsFeed.MEDIA_TYPE || link.type == OpdsPublication.MEDIA_TYPE) {
//                    links.add(link.href)
//                }
//            }
//        }
//
//        // Add links from facets
//        feed.facets?.forEach { facet ->
//            facet.links.forEach { link ->
//                if (link.type == OpdsFeed.MEDIA_TYPE || link.type == OpdsPublication.MEDIA_TYPE) {
//                    links.add(link.href)
//                }
//            }
//        }

        // Process each link
        for (href in links) {
            val resolvedUrl = resolveUrl(baseUrl, href)

            // Skip if we've already visited this URL to prevent loops
            if (resolvedUrl in visitedUrls) {
                continue
            }

            // Mark as visited to prevent loops
            visitedUrls.add(resolvedUrl)

            try {
                // Fetch the content
                val content = fetchContent(resolvedUrl)

                // Validate the content
                when (val result = validateOpdsContent(content, resolvedUrl, true, warnings)) {
                    is ValidationResult.Error -> {
                        errors.add("Errors in linked feed at $resolvedUrl:")
                        errors.addAll(result.errors.map { "  - $it" })
                    }
                    is ValidationResult.Exception -> {
                        errors.add("Exception validating linked feed at $resolvedUrl: ${result.message}")
                    }
                    is ValidationResult.Success -> {
                        // If it's a feed, follow its links recursively
                        if (result.feedType == FeedType.FEED) {
                            val linkedFeed = json.decodeFromString<OpdsFeed>(content)
                            followLinks(linkedFeed, resolvedUrl, visitedUrls, errors, warnings, depth + 1)
                        }
                    }
                }
            } catch (e: Exception) {
                errors.add("Error fetching or processing linked feed at $resolvedUrl: ${e.message}")
            }
        }
    }

    /**
     * Resolves a URL relative to a base URL.
     * @param baseUrl The base URL
     * @param href The relative or absolute URL
     * @return The resolved absolute URL
     */
    private fun resolveUrl(baseUrl: String, href: String): String {
        // If it's already an absolute URL, return it as is
        if (href.startsWith("http://") || href.startsWith("https://") || href.startsWith("file://")) {
            return href
        }

        // Otherwise, resolve it relative to the base URL
        try {
            val baseUri = URI(baseUrl)
            return baseUri.resolve(href).toString()
        } catch (e: Exception) {
            // If resolution fails, just concatenate
            return if (baseUrl.endsWith("/")) {
                baseUrl + href
            } else {
                "$baseUrl/$href"
            }
        }
    }

    /**
     * Fetches content from a URL.
     * @param url The URL to fetch content from
     * @return The content as a string
     */
    private fun fetchContent(url: String): String {
        return when {
            url.startsWith("file://") -> {
                val path = URI(url).path
                File(path).readText()
            }
            url.startsWith("http://") || url.startsWith("https://") -> {
                val connection = URL(url).openConnection()
                connection.connectTimeout = 10000 // 10 seconds
                connection.readTimeout = 10000 // 10 seconds
                connection.getInputStream().bufferedReader().use { it.readText() }
            }
            else -> throw IllegalArgumentException("Unsupported URL scheme: $url")
        }
    }

    /**
     * Validates an OPDS feed against the specification requirements.
     * According to OPDS 2.0, a valid feed must:
     * - Contain at least one collection identified by navigation, publications, or groups
     * - Contain a title in its metadata
     * - Contain a reference to itself using a self link
     * @param feed The feed to validate
     * @return A Result object containing either Unit (success) or a ValidationException (failure)
     */
    fun validateFeed(feed: OpdsFeed, warnings: MutableList<String> = mutableListOf()): Result<Unit> {
        val errors = mutableListOf<String>()

        // Check for recommended but not required fields
        if (feed.metadata.description == null) {
            warnings.add("Feed should include a description")
        }

        // Ensure metadata and links are not null and valid
        if (feed.metadata.title.isBlank()) {
            errors.add("Feed metadata must contain a non-blank title")
        }

        // Links contains rel = self
        val hasSelfLink = feed.links.any { it.hasRel("self") }
        if (!hasSelfLink) {
            errors.add("Feed must contain at least one 'self' link")
        }

        // At least one required collection
        if (feed.navigation == null && feed.publications == null && feed.groups == null) {
            errors.add("Feed must contain at least one of: navigation, publications, or groups")
        }

        // Validate contributors (author, translator, editor, etc.)
        validateContributors(feed.metadata, errors)

        return if (errors.isEmpty()) {
            Result.success(Unit)
        } else {
            Result.failure(ValidationException(errors))
        }
    }

    /**
     * Validates contributors (author, translator, publisher) in the metadata.
     * @param metadata The metadata to validate
     * @param errors The list of errors to collect validation issues
     */
    private fun validateContributors(metadata: OpdsFeedMetadata, errors: MutableList<String>) {
        // Publisher should be a string (non-blank)
        metadata.publisher?.let {
            if (it.isBlank()) {
                errors.add("Publisher cannot be blank")
            }
        }

    }

    /**
     * Validates an OPDS publication against the specification requirements.
     * In RESPECT context, each publication represents a Learning Unit.
     * According to OPDS 2.0, a valid publication must:
     * - Contain at least one acquisition link
     * - Should contain a self link
     * - When images are present, at least one must be in a required format
     * @param publication The publication to validate
     * @param warnings List to collect warning messages
     * @return A Result object containing either Unit (success) or a ValidationException (failure)
     */
    fun validatePublication(
        publication: OpdsPublication,
        warnings: MutableList<String> = mutableListOf()
    ): Result<Unit> {
        val errors = mutableListOf<String>()

        // Check that metadata and links are not null or empty
        if (publication.metadata.title.isBlank()) {
            errors.add("Publication metadata must contain a non-blank title")
        }

        if (publication.metadata.identifier.isBlank()) {
            errors.add("Publication metadata must contain a non-blank identifier")
        }

        if (publication.links.isEmpty()) {
            errors.add("Publication must contain at least one link")
        }

        // Check for acquisition links using constants
        val hasAcquisitionLink = publication.links.any { link ->
            ACQUISITION_RELATIONS.any { rel -> link.rel?.contains(rel) == true }
        }

        if (!hasAcquisitionLink) {
            errors.add("Publication must contain at least one acquisition link")
        }

        // Check for self link (warning only, doesn't add to errors)
        val hasSelfLink = publication.links.any { link -> link.rel?.contains("self") == true }
        if (!hasSelfLink) {
            warnings.add("Publication should contain a self link")
        }

        // Check image formats when images are present
        if (publication.images != null && publication.images.isNotEmpty()) {
            val hasRequiredImageFormat = publication.images.any { link ->
                val type = link.type
                type == "image/jpeg" || type == "image/avif" ||
                        type == "image/png" || type == "image/gif"
            }

            if (!hasRequiredImageFormat) {
                errors.add("Publications with images must include at least one image in JPEG, AVIF, PNG, or GIF format")
            }
        }

        return if (errors.isEmpty()) {
            Result.success(Unit)
        } else {
            Result.failure(ValidationException(errors))
        }
    }

    /**
     * Determines if a JSON object is more likely to be a feed based on its structure.
     * @param jsonObject The JSON object to analyze
     * @return true if the object is more likely to be a feed, false otherwise
     */
    private fun isProbablyFeed(jsonObject: JsonObject): Boolean {
        // Check for feed-specific collections
        val hasNavigation = jsonObject.containsKey("navigation")
        val hasPublications = jsonObject.containsKey("publications")
        val hasGroups = jsonObject.containsKey("groups")
        val hasFacets = jsonObject.containsKey("facets")

        // If it has any feed-specific collections, it's likely a feed
        return hasNavigation || hasPublications || hasGroups || hasFacets
    }

    //Represents the type of OPDS content
    enum class FeedType {
        FEED,
        PUBLICATION
    }

    /**
     * Represents the result of validation
     */
    sealed class ValidationResult {
        //Validation successful
        data class Success(
            val feedType: FeedType,
            val warnings: List<String> = emptyList()
        ) : ValidationResult()

        //Validation failed with errors
        data class Error(val errors: List<String>) : ValidationResult()

        //Exception occurred during validation
        data class Exception(
            val message: String,
            val exception: Throwable
        ) : ValidationResult()
    }

    /**
     * Exception thrown when validation fails.
     */
    class ValidationException(val errors: List<String>) : Exception(errors.joinToString("\n"))

    companion object {
        // Constants for OPDS acquisition relations
        const val ACQUISITION = "http://opds-spec.org/acquisition"
        const val ACQUISITION_OPEN_ACCESS = "http://opds-spec.org/acquisition/open-access"
        const val ACQUISITION_BORROW = "http://opds-spec.org/acquisition/borrow"
        const val ACQUISITION_BUY = "http://opds-spec.org/acquisition/buy"
        const val ACQUISITION_SAMPLE = "http://opds-spec.org/acquisition/sample"
        const val ACQUISITION_PREVIEW = "preview"
        const val ACQUISITION_SUBSCRIBE = "http://opds-spec.org/acquisition/subscribe"

        // List of all acquisition relations
        val ACQUISITION_RELATIONS = listOf(
            ACQUISITION,
            ACQUISITION_OPEN_ACCESS,
            ACQUISITION_BORROW,
            ACQUISITION_BUY,
            ACQUISITION_SAMPLE,
            ACQUISITION_PREVIEW,
            ACQUISITION_SUBSCRIBE
        )

        // Maximum depth for link following to prevent excessive recursion
        const val MAX_LINK_DEPTH = 5
    }

}