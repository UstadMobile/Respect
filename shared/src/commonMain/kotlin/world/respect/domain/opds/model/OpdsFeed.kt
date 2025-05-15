package world.respect.domain.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents an OPDS 2.0 Catalog Feed.
 *
 * According to the specification, an OPDS 2.0 Catalog Feed must:
 * - Contain at least one collection (navigation, publications or groups)
 * - Contain a title in its metadata
 * - Contain a reference to itself using a self link
 * For reference, see the schema: https://drafts.opds.io/schema/feed.schema.json
 */
@Serializable
data class OpdsFeed(
    val metadata: OpdsFeedMetadata,               // Required metadata field
    val links: List<OpdsLink>,             // Required
    val publications: List<OpdsPublication>? = null,  // Optional, but must exist if provided
    val navigation: List<OpdsLink>? = null,  // Optional
    val facets: List<OpdsFacet>? = null,   // Optional
    val groups: List<OpdsGroup>? = null   // Optional groups collection
) {
    companion object {
        const val MEDIA_TYPE = "application/opds+json"
    }
}