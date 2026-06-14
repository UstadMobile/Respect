package world.respect.xapi.ipc.shared.messages

import android.os.Bundle

/**
 * Immutable data class to store a message: Message objects are obtained from an object pool as per
 * the Android documentation.
 *
 * https://developer.android.com/reference/android/os/Message
 */
data class MessageData(
    val data: Bundle,
    val what: Int,
    val arg1: Int,
    val arg2: Int,
)