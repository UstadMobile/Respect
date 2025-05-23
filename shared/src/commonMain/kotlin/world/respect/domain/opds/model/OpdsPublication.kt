package world.respect.domain.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents a Publication in an OPDS 2.0 catalog.
 * In RESPECT context, each publication represents a Learning Unit.
 *
 * For reference, see the schema: https://drafts.opds.io/schema/publication.schema.json
 */
@Serializable
data class OpdsPublication(
    val metadata: ReadiumMetadata,
    val links: List<OpdsLink>,
    val images: List<OpdsLink>? = null
) {
    companion object {
        const val MEDIA_TYPE = "application/opds-publication+json"
    }
}
