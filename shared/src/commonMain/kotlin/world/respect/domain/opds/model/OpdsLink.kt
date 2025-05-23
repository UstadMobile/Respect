package world.respect.domain.opds.model

import kotlinx.serialization.Serializable
import world.respect.domain.opds.serialization.StringListSerializer

/**
 * Represents a Link Object in OPDS 2.0.
 * The href field is required, all other fields are optional.
 *
 * For reference, see the schema: https://readium.org/webpub-manifest/schema/link.schema.json
 */
@Serializable
data class OpdsLink(
    val href: String,
    val rel: OpdsRel? = null,
    val type: String? = null,
    val title: String? = null,
    val templated: Boolean? = null,
    val properties: OpdsLinkProperties? = null,
    val height: Int? = null,
    val width: Int? = null,
    val size: Int? = null,
    val bitrate: Double? = null,
    val duration: Double? = null,

    //As per the spec: language can be a single string or list.
    @Serializable(with = StringListSerializer::class)
    val language: List<String>? = null,
    val alternate: List<OpdsLink>? = null,
    val children: List<OpdsLink>? = null,
    val subcollections: List<OpdsLink>? = null
) {
    /**
     * Checks if this link contains the specified relation.
     */
    fun hasRel(relation: String): Boolean = rel?.contains(relation) == true
}
