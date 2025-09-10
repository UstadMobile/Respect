package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource

fun PagingSource.LoadParams<*>.toPrettyString() : String {
    val typeName = when(this) {
        is PagingSource.LoadParams.Refresh -> "Refresh"
        is PagingSource.LoadParams.Append -> "Append"
        is PagingSource.LoadParams.Prepend -> "Prepend"
    }

    return "$typeName(key=$key, loadSize=$loadSize)"
}