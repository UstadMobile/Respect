package world.respect.domain.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents a contributor (author, translator, etc.) with optional links.
 *
 * For reference, see the schema: https://drafts.opds.io/opds-2.0#22-publications
 */
@Serializable
data class OpdsContributor(
    val name: String,                      // Required field (name of the contributor)
    val identifier: String? = null,        // Optional field (identifier for the contributor)
    val sortAs: String? = null,            // Optional field (sorting name for the contributor)
    val links: List<OpdsLink>? = null      // Optional field (links to further information)
)

@Serializable
sealed class OpdsContributorField {
    @Serializable
    data class ContributorName(val name: String) : OpdsContributorField()

    @Serializable
    data class ContributorObject(val contributor: OpdsContributor) : OpdsContributorField()
}
