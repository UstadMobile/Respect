package org.openeel.lib.ipc.messagebridge

import android.os.Messenger

/**
 * Service connection is fast, but asynchronous when done using a service connection. In testing
 * a ServiceRule is used instead of a service connection.
 */
fun interface MessengerProvider {

    operator fun invoke(): Messenger

}