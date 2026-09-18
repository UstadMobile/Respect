package world.respect.datalayer.repository.flow

import io.ktor.http.Headers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import world.respect.datalayer.repository.ext.copyToValidateOnRemote
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.combineWithRemote
import world.respect.lib.dataloadstate.ext.responseETagAndLastModified
import world.respect.lib.dataloadstate.ext.isStillValid

const val LOADED_DECK_SIZE = 4

fun <T: Any> Flow<DataLoadState<T>>.asRepoFlow(
    dataLoadParams: DataLoadParams,
    remoteFlow: (DataLoadParams) -> Flow<DataLoadState<T>>,
    onRemoteUpdate: suspend (DataReadyState<T>) -> Unit,
): Flow<DataLoadState<T>> {
    return channelFlow {
        val remoteFlowState = MutableStateFlow<DataLoadState<T>>(DataLoadingState())

        val sharedLocal = this@asRepoFlow.shareIn(
            scope = this, started = SharingStarted.Lazily
        ).distinctUntilChanged { old, new ->
            old.metaInfo.headers.responseETagAndLastModified().isStillValid(
                other = new.metaInfo.headers.responseETagAndLastModified()
            )
        }

        launch {
            sharedLocal.combine(remoteFlowState) { localState, remoteState ->
                localState.combineWithRemote(remoteState)
            }.collect {
                send(it)
            }
        }

        launch {
            val remoteLoadedDeck = ArrayDeque<Headers>(LOADED_DECK_SIZE)
            sharedLocal.filter { newLocalState ->
                /* When new remote data is loaded, this normally leads to updating the local
                 * datasource, which then leads to the local flow emitting the new data. We want
                 * to avoid triggering _another_ request to the remote datasource for the same data
                 * we just received from it.
                 *
                 * This is done by keeping the last few headers.
                 */
                val newLocalStateEtagAndLastModified = newLocalState.metaInfo.headers
                    .responseETagAndLastModified()

                !remoteLoadedDeck.any { prevRemoteHeaders ->
                    prevRemoteHeaders.responseETagAndLastModified().isStillValid(
                        other = newLocalStateEtagAndLastModified
                    )
                }
            }.collectLatest { localState ->
                remoteFlow(
                    dataLoadParams.copyToValidateOnRemote(localState.metaInfo)
                ).collect { remoteState ->
                    remoteFlowState.value = remoteState

                    if(remoteState is DataReadyState) {
                        onRemoteUpdate(remoteState)
                        if(remoteLoadedDeck.size > LOADED_DECK_SIZE) {
                            remoteLoadedDeck.removeLast()
                        }
                        remoteLoadedDeck.addFirst(remoteState.metaInfo.headers)
                    }
                }
            }
        }
    }
}