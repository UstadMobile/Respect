package world.respect.domain.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents a Link Object in OPDS 2.0.
 * The href field is required, all other fields are optional.
 *
 * For reference, see the schema: https://readium.org/webpub-manifest/schema/link.schema.json
 */
@Serializable
data class OpdsLink(
    val href: String,
    val rel: OpdsRel? = null,              // Optional field (defines the relationship)
    val type: String? = null,              // Optional field (type of the linked resource)
    val title: String? = null,             // Optional field (title of the linked resource)
    val templated: Boolean? = null,        // Optional field (indicates if the link is templated)
    val properties: OpdsLinkProperties? = null, // Optional additional properties for the link
    val height: Int? = null,               // Optional field (height for image links)
    val width: Int? = null,                // Optional field (width for image links)
    val size: Int? = null,                 // Optional field (original resource size in bytes)
    val bitrate: Double? = null,           // Optional field (bitrate in kbps)
    val duration: Double? = null,          // Optional field (duration in seconds)
    val language: List<String>? = null,    // Optional field (expected language(s))
    val alternate: List<OpdsLink>? = null, // Optional field (alternate resources)
    val children: List<OpdsLink>? = null,   // Optional field (child resources)
    val subcollections: List<OpdsLink>? = null // Added to handle nested collections (subcollections)
) {
    /**
     * Checks if this link contains the specified relation.
     */
    fun hasRel(relation: String): Boolean = rel?.contains(relation) == true
}
