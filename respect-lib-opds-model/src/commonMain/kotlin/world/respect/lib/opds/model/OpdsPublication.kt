package world.respect.lib.opds.model

import kotlinx.serialization.Serializable

/**
 * As per the spec:
 * https://drafts.opds.io/opds-2.0#51-opds-publication
 *
 * It is a Readium Web Publication without the requirement to include reading order, that must have
 * at least one acquisition link, and should contain a self link.
 *
 * See also the Readium Web Publication spec: https://github.com/readium/webpub-manifest
 *
 * In RESPECT context, each publication represents a Learning Unit.
 *
 * This model is called 'OpdsPublication', but it also handles the Readium Web Publication Manifest
 *
 * OPDS publication schema: https://drafts.opds.io/schema/publication.schema.json
 * Readium publication schema: https://readium.org/webpub-manifest/schema/publication.schema.json
 */
@Serializable
data class OpdsPublication(
    val metadata: ReadiumMetadata,
    val links: List<ReadiumLink>,
    val images: List<ReadiumLink>? = null,
    val readingOrder: List<ReadiumLink>? = null,
    val resources: List<ReadiumLink>? = null,
    val toc: List<ReadiumLink>? = null,
) {
    companion object {
        const val MEDIA_TYPE = "application/opds-publication+json"

        const val MEDIA_TYPE_READIUM_MANIFEST = "application/webpub+json"
    }
}
