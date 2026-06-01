package world.respect.xapi.ipc.client

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.xapi.ipc.shared.messages.XapiIpcWhatFlags

/**
 * XapiResourceIpcClient must host an interface implementation for messages for which a reply is
 * expected e.g.
 *
 * returns response
 * suspend fun sendRequest(request: Message): Message
 *  .. sends message using the messenger, waits for reply by using a deferred completable.
 *
 */
class XapiResourceIpcClient(
    private val messenger: Messenger,
): XapiResource {

    //This is receiving responses.
    class IncomingHandler: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            //Here: try and figure out where it's going
            msg.sendingUid
            when(msg.what) {

                XapiIpcWhatFlags.WHAT_GET_STATEMENTS -> {

                }
            }


            super.handleMessage(msg)
        }
    }


    override val statements: XapiStatementsResource by lazy {
        XapiStatementsResourceIpcClient(messenger)
    }

}