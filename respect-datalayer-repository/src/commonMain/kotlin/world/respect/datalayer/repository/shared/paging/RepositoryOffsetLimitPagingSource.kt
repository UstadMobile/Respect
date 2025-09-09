package world.respect.datalayer.repository.shared.paging

import androidx.paging.PagingSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import world.respect.datalayer.shared.paging.FilterPagingSource
import world.respect.datalayer.shared.paging.CacheableHttpPagingSource
import io.github.aakira.napier.Napier

/**
 * PagingSource based on an offline-first repository.
 *
 * @param onUpdateLocalFromRemote function (e.g. provided by LocalModeDataSource) that will store
 *        newly received remote data in the local datasource.
 */
class RepositoryOffsetLimitPagingSource<T: Any>(
    internal val local: PagingSource<Int, T>,
    internal val remote: PagingSource<Int, T>,
    private val onUpdateLocalFromRemote: suspend (List<T>) -> Unit,
    mediatorStore: PagingSourceMediatorStore,
    argKey: Int,
    tag: String? = null,
) : FilterPagingSource<Int, T>(
    src = local,
    tag = tag,
){

    val scope = CoroutineScope(Dispatchers.Default + Job())

    val remoteMediator = mediatorStore.getOrCreateMediator(argKey) {
        DoorOffsetLimitRemoteMediator { offset, limit ->
            val remoteLoadResult = remote.load(
                LoadParams.Refresh(offset, limit, false)
            )

            withContext(NonCancellable) {
                if(remoteLoadResult is LoadResult.Page) {
                    onUpdateLocalFromRemote(remoteLoadResult.data)
                }

                val isNotModifiedResponse = (remoteLoadResult as? LoadResult.Error)?.throwable is
                        CacheableHttpPagingSource.NotModifiedNonException

                if(remoteLoadResult is LoadResult.Page || isNotModifiedResponse) {
                    @Suppress("UNCHECKED_CAST")
                    (remote as? CacheableHttpPagingSource<Int, T>)?.onLoadResultStored(remoteLoadResult)
                }
            }
        }
    }

    init {
        this.registerInvalidatedCallback { scope.cancel() }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        Napier.d("RepositoryOffsetLimitPagingSource: tag=$tag load key=${params.key}")
        val localResult = super.load(params)
        remoteMediator.onLoad(params)
        return localResult
    }

}