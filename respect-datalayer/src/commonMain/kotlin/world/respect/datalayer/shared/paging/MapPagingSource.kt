package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.aakira.napier.Napier

/**
 * Map a PagingSource where there is a 1:1 transform between T and R (e.g. from database entities to
 * model classes)
 */
internal class MapPagingSource<T: Any, R: Any>(
    private val src: PagingSource<Int, T>,
    private val transform: (T) -> R,
    tag: String? = null
): DelegatedInvalidationPagingSource<Int, R>(src, tag) {

    val logPrefix = "MapPagingSource(tag=$tag)"

    override fun getRefreshKey(state: PagingState<Int, R>): Int? {
        return state.getClippedRefreshKey()
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, R> {
        Napier.d("$logPrefix: load ${params.toPrettyString()}")

        registerInvalidationCallbackIfNeeded()
        val srcResult = src.load(params)

        return when (srcResult) {
            is LoadResult.Page -> {
                LoadResult.Page(
                    data = srcResult.data.map(transform),
                    prevKey = srcResult.prevKey,
                    nextKey = srcResult.nextKey,
                    itemsAfter = srcResult.itemsAfter,
                    itemsBefore = srcResult.itemsBefore
                )
            }

            is LoadResult.Error -> {
                Napier.e("$logPrefix: ERROR loading ${params.toPrettyString()}: error", throwable = srcResult.throwable)
                LoadResult.Error(srcResult.throwable)
            }

            is LoadResult.Invalid -> {
                Napier.e("$logPrefix: INVALID loading ${params.toPrettyString()}")
                LoadResult.Invalid()
            }
        }
    }
}

fun <T: Any, R: Any> PagingSource<Int, T>.map(
    tag: String? = null,
    transform: (T) -> R
): PagingSource<Int, R> {
    return MapPagingSource(this, transform, tag)
}
