package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource

fun <Key: Any, Value: Any> PagingSource<Key, Value>.asIPagingSourceFactory(): IPagingSourceFactory<Key, Value> {
    return IPagingSourceFactory { this }
}
