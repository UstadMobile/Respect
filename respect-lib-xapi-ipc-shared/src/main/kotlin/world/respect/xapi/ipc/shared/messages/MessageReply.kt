package world.respect.xapi.ipc.shared.messages

import android.os.Bundle

/**
 * Immutable class to store a reply: once a handler is done, the bundle can be mutated/reset.
 */
data class MessageReply(
    val data: Bundle,
    val what: Int,
    val arg1: Int,
    val arg2: Int,
)