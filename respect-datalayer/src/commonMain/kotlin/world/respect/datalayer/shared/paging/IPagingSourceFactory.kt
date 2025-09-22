package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource

fun interface IPagingSourceFactory<Key: Any, Value: Any> {

    operator fun invoke(): PagingSource<Key, Value>

}
