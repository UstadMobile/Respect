package world.respect.domain.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents a Facet group in an OPDS 2.0 catalog.
 *
 * For reference, see the schema: https://drafts.opds.io/opds-2.0#24-facets
 */
@Serializable
data class OpdsFacet(
    val metadata: OpdsFeedMetadata,        // Required field (metadata about the facet group)
    val links: List<OpdsLink>              // Required field (links related to the facet)
)