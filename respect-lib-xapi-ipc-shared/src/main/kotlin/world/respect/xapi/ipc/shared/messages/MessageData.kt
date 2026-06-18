package world.respect.xapi.ipc.shared.messages

import android.os.Bundle
import android.os.Message

/**
 * Immutable data class to store a message: Message objects are obtained from an object pool as per
 * the Android documentation.
 *
 * https://developer.android.com/reference/android/os/Message
 */
data class MessageData(
    val data: Bundle,
    val what: Int,
    val arg1: Int = 0,
    val arg2: Int = 0,
) {

    constructor(message: Message) : this(
        data = message.data, what = message.what, arg1 = message.arg1, arg2 = message.arg2
    )

}