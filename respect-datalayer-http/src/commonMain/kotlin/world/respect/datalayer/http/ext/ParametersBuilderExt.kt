package world.respect.datalayer.http.ext

import io.ktor.http.ParametersBuilder
import world.respect.datalayer.DataLayerParams
import world.respect.datalayer.shared.params.GetListCommonParams

fun ParametersBuilder.appendIfNotNull(
    name: String,
    value: String?
) {
    value?.also { append(name, it) }
}

fun ParametersBuilder.appendCommonListParams(
    params: GetListCommonParams
) {
    appendIfNotNull(DataLayerParams.GUID, params.guid)
    appendIfNotNull(DataLayerParams.SINCE, params.since?.toString())
    appendIfNotNull(DataLayerParams.SEARCH_QUERY, params.searchQuery)
    appendIfNotNull(DataLayerParams.INCLUDE_DELETED, params.includeDeleted?.toString())
}
