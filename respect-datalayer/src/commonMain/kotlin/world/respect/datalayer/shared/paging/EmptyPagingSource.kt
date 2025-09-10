package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

class EmptyPagingSource<Key: Any, Value: Any>: PagingSource<Key, Value>() {

    override fun getRefreshKey(state: PagingState<Key, Value>): Key? {
        return null
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override suspend fun load(
        params: LoadParams<Key>
    ): LoadResult<Key, Value> {
        return LoadResult.Page<Key, Value>(emptyList(), null, null)
    }
}

