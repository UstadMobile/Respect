package com.ustadmobile.libcache.util

import kotlinx.coroutines.channels.Channel

/**
 * Receive any pending ready to consume items up to a limit of maxItems
 */
fun <T> Channel<T>.receivePending(
    maxItems: Int = Int.MAX_VALUE
) : List<T> {
    val list = mutableListOf<T>()
    var itemCount = 0

    do {
        val result = tryReceive()
        val item = result.getOrNull()
        item?.also { list.add(it) }
    } while (item != null && itemCount++ < maxItems)

    return list.toList()
}
