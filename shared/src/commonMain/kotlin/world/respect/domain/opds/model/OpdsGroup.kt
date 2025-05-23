package world.respect.domain.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents a Group in an OPDS 2.0 catalog.
 * A group must contain either a navigation collection or a publications collection.
 *
 * For reference, see the schema: https://drafts.opds.io/opds-2.0#25-groups
 */
@Serializable
data class OpdsGroup(
    val metadata: OpdsFeedMetadata,        // Required field (metadata about the group)
    val links: List<OpdsLink>? = null,     // Optional field (links related to the group)
    val navigation: List<OpdsLink>? = null, // Optional field (navigation links for the group)
    val publications: List<OpdsPublication>? = null   // Optional field (publications in the group)
)