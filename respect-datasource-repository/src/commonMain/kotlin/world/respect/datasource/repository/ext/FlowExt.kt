package world.respect.datasource.repository.ext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import world.respect.datasource.DataErrorResult
import world.respect.datasource.DataLoadMetaInfo
import world.respect.datasource.DataLoadResult
import world.respect.datasource.DataLoadState
import world.respect.datasource.DataLoadingState

fun <T: Any> DataLoadState<T>.copyLoadState(
    metaInfo: DataLoadMetaInfo = this.metaInfo,
    localMetaInfo: DataLoadMetaInfo? = this.localMetaInfo,
    remoteMetaInfo: DataLoadMetaInfo? = this.remoteMetaInfo,
) : DataLoadState<T> {
    return when(this) {
        is DataLoadResult -> copy(
            metaInfo = metaInfo,
            localMetaInfo = localMetaInfo,
            remoteMetaInfo = remoteMetaInfo,
        )
        is DataLoadingState -> copy(
            metaInfo = metaInfo,
            localMetaInfo = localMetaInfo,
            remoteMetaInfo = remoteMetaInfo,
        )
        is DataErrorResult -> copy(
            metaInfo = metaInfo,
            localMetaInfo = localMetaInfo,
            remoteMetaInfo = remoteMetaInfo,
        )
    }
}

/**
 * Given a local datasource flow and a remote datasource flow, combine the two to provide an
 * offline-first datasource for the UI.
 *spo
 * Any locally available data can therefor displayed immediately whilst the remote datasource checks
 * for updates if/as required or possible.
 *
 * @receiver the DataLoadState flow from the local data source
 * @param remoteFlow the DataLoadState flow from the remote data source
 * @param onRemoteNewer a function that will be invoked when the remote flow data is newer than the
 *        local data e.g. to update the local datasource.
 */
fun <T: Any> Flow<DataLoadState<T>>.combineLocalWithRemote(
    remoteFlow: Flow<DataLoadState<T>>,
    onRemoteNewer: suspend (DataLoadResult<T>) -> Unit,
): Flow<DataLoadState<T>> {
    val mutex = Mutex()

    return combine(remoteFlow) { local, remote ->
        if(local is DataLoadResult && remote is DataLoadResult
            && remote.data != null
            && local.metaInfo.lastModified < remote.metaInfo.lastModified
        ) {
            mutex.withLock {
                println("Updating local")
                onRemoteNewer(remote)
            }
        }

        local.copyLoadState(
            metaInfo = local.metaInfo.copy(
                status = remote.metaInfo.status
            ),
            localMetaInfo = local.metaInfo,
            remoteMetaInfo = remote.metaInfo,
        )
    }
}
