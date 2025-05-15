package world.respect.domain.opds.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Represents metadata for a Publication.
 * For reference, see the schema:  https://readium.org/webpub-manifest/schema/metadata.schema.json
 */
@Serializable
data class OpdsPublicationMetadata(
    val title: Map<String, String>,                     // Title with language map (language code as key, title as value)
    @SerialName("@type")
    val type: String? = null,               // Optional field (type of publication)
    val identifier: String,                 // Required field (unique identifier for the publication)
    //TODO: OpdsContributor is polymorphic ; can be just a string
    val author: OpdsContributorField? = null,    // Optional field (author of the publication)
    val translator: OpdsContributorField? = null, // Optional field (translator of the publication)
    val editor: OpdsContributorField? = null,     // Optional field (editor of the publication)
    val artist: OpdsContributorField? = null,     // Optional field (artist of the publication)
    val illustrator: OpdsContributorField? = null, // Optional field (illustrator of the publication)
    val letterer: OpdsContributorField? = null,   // Optional field (letterer of the publication)
    val penciler: OpdsContributorField? = null,   // Optional field (penciler of the publication)
    val colorist: OpdsContributorField? = null,   // Optional field (colorist of the publication)
    val inker: OpdsContributorField? = null,      // Optional field (inker of the publication)
    val narrator: OpdsContributorField? = null,   // Optional field (narrator of the publication)
    val publisher: String? = null,          // Optional field (publisher of the publication)
    val language: String? = null,           // Optional field (language of the publication)
    val modified: String? = null,           // Optional field (last modification date)
    val description: String? = null,        // Optional field (description of the publication)
    val belongsTo: OpdsBelongsTo? = null,    // Optional field (indicates series or collection)
    val readingDirection: String? = null,    // Optional field (reading direction)
    val numberOfPages: Int? = null,          // Optional field (number of pages)
    val subject: List<String>? = null,       // Optional field (list of subjects)
    val numberOfItems: Int? = null,          // Optional field (total number of items)
    val itemsPerPage: Int? = null,           // Optional field (items per page)
    val currentPage: Int? = null,            // Optional field (current page)
    val images: List<OpdsLink>? = null       // Optional field (images related to the publication)
)