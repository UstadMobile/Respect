package world.respect.domain.opds.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Represents metadata for an OPDS catalog or collection.
 * This includes details like title, number of items, items per page, etc.
 *
 * For reference, see the schema: https://drafts.opds.io/schema/feed-metadata.schema.json
 */
@Serializable
data class OpdsFeedMetadata(
    val title: String,                     // Required field (title of the catalog or collection)
    val identifier: String? = null,        // Optional (identifier in URI format)
    @SerialName("@type")
    val type: String? = null,              // Optional (type in URI format)
    val subtitle: String? = null,          // Optional (subtitle of the catalog or collection)
    val numberOfItems: Int? = null,        // Optional (total number of items in the collection)
    val itemsPerPage: Int? = null,         // Optional (items per page in the collection)
    val currentPage: Int? = null,          // Optional (current page number)

    val publisher: String? = null,         // Publisher is a simple String (e.g., "SciFi Publishing Inc.")

    //TODO: These need to be polymorphic. As per the spec; it can be a string, or an object of its own
    val author: OpdsContributorField? = null,   // Optional contributor field (can be multiple, but for now it's just one)
    val translator: OpdsContributorField? = null,  // Optional contributor field
    val editor: OpdsContributorField? = null,   // Optional contributor field

    val language: String? = null,          // Optional language
    val description: String? = null,       // Optional description
    @Contextual
    val modified: LocalDateTime? = null        // Optional modified date
)