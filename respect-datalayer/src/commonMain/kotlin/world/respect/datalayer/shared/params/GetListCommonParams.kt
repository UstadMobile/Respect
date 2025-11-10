package world.respect.datalayer.shared.params

import io.ktor.util.StringValues
import world.respect.datalayer.DataLayerParams
import kotlin.time.Instant

/**
 * Common parameters used on most RESPECT-ext get list endpoints
 */
data class GetListCommonParams(
    val guid: String? = null,
    val searchQuery: String? = null,
    val since: Instant? = null,
) {

    companion object {

        fun fromParams(stringValues: StringValues) : GetListCommonParams{
            return GetListCommonParams(
                guid = stringValues[DataLayerParams.GUID],
                searchQuery = stringValues[DataLayerParams.SEARCH_QUERY],
                since = stringValues[DataLayerParams.SINCE]?.let { Instant.parse(it) },
            )
        }

    }

}