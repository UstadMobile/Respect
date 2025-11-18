package world.respect.datalayer.shared.params

import io.ktor.util.StringValues
import world.respect.datalayer.DataLayerParams
import kotlin.time.Instant

/**
 * Common parameters used on most RESPECT-ext get list endpoints
 *
 * @param includeDeleted if non-null and true, then entities where status is set to TO_BE_DELETED are
 *        included, otherwise false
 */
data class GetListCommonParams(
    val guid: String? = null,
    val searchQuery: String? = null,
    val since: Instant? = null,
    val includeDeleted: Boolean? = null,
) {

    companion object {

        fun fromParams(stringValues: StringValues) : GetListCommonParams{
            return GetListCommonParams(
                guid = stringValues[DataLayerParams.GUID],
                searchQuery = stringValues[DataLayerParams.SEARCH_QUERY],
                since = stringValues[DataLayerParams.SINCE]?.let { Instant.parse(it) },
                includeDeleted = stringValues[DataLayerParams.INCLUDE_DELETED]?.toBoolean(),
            )
        }

    }

}