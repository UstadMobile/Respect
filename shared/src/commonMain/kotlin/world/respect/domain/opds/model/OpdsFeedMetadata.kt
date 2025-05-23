package world.respect.domain.opds.model

import com.eygraber.uri.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OPDS Feed Metadata.
 *
 * This includes details like title, number of items, items per page, etc.
 *
 * For reference, see the schema: https://drafts.opds.io/schema/feed-metadata.schema.json
 */
@Serializable
data class OpdsFeedMetadata(
    val identifier: Uri? = null,

    @SerialName("@type")
    val type: String? = null,

    val title: String,

    val subtitle: String? = null,

    //TODO: Should be date/datetime
    val modified: String? = null,

    val description: String? = null,

    val itemsPerPage: Int? = null,

    val currentPage: Int? = null,

    val numberOfItems: Int? = null,

)