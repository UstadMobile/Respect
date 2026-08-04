package com.ustadmobile.libcache.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Receive any pending ready to consume items up to a limit of maxItems
 */
fun <T> Channel<T>.receivePending(
    maxItems: Int = Int.MAX_VALUE,
    backlogCount: MutableStateFlow<Int>?,
) : List<T> {
    val list = mutableListOf<T>()
    var itemCount = 0

    do {
        val result = tryReceive()
        val item = result.getOrNull()
        item?.also { list.add(it) }
    } while (item != null && itemCount++ < maxItems)

    if(itemCount > 0) {
        backlogCount?.update { it - itemCount }
    }

    return list.toList()
}

fun <T> Channel<T>.trySendAndUpdateBacklogSize(
    element: T,
    backlogCount: MutableStateFlow<Int>
) : ChannelResult<Unit> {
    return trySend(element).also { result ->
        if(result.isSuccess) {
            backlogCount.update { it + 1 }
        }
    }
}

suspend fun <T> Channel<T>.sendAndUpdateBacklogSize(
    element: T,
    backlogCount: MutableStateFlow<Int>
)  {
    backlogCount.update { it + 1 }
    send(element)
}

suspend fun <T> Channel<T>.receiveAndUpdateBacklogSize(
    backlogCount: MutableStateFlow<Int>
) : T {
    val item = receive()
    backlogCount.update { it - 1 }
    return item
}
