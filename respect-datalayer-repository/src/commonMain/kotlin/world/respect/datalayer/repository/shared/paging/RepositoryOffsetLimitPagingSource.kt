package world.respect.datalayer.repository.shared.paging

import androidx.paging.PagingSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import world.respect.datalayer.shared.paging.FilterPagingSource
import io.github.aakira.napier.Napier

/**
 * PagingSource based on an offline-first repository.
 *
 * @param onUpdateLocalFromRemote function (e.g. provided by LocalModeDataSource) that will store
 *        newly received remote data in the local datasource.
 * @param Local the local data type (may use summary or derivative data from SQL queries etc)
 */
class RepositoryOffsetLimitPagingSource<Local: Any>(
    internal val local: PagingSource<Int, Local>,
    val remoteMediator: DoorOffsetLimitRemoteMediator,
    tag: String? = null,
) : FilterPagingSource<Int, Local>(
    src = local,
    tag = tag,
){

    private val logPrefix = "RPaging/RepositoryOffsetLimitPagingSource(tag = $tag):"

    val scope = CoroutineScope(Dispatchers.Default + Job())

    init {
        this.registerInvalidatedCallback {
            Napier.d("$logPrefix : invalidated" )
            scope.cancel()
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Local> {
        Napier.d("$logPrefix load key=${params.key}")
        val localResult = super.load(params)
        remoteMediator.onLoad(params)
        return localResult
    }

}