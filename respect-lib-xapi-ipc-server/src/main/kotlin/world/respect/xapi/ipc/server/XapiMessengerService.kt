package world.respect.xapi.ipc.server

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.xapi.ipc.shared.messages.XapiIpcWhatFlags

/**
 * Messenger service (server) that will receive xAPI requests, send them to a given xAPI resource,
 * and then send a reply message with the response.
 *
 * The replyTo field of the incoming message MUST be a Messenger of the client where responses
 * should be sent.
 *
 * See
 * https://developer.android.com/develop/background-work/services/bound-services#Messenger
 * https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example/android/apis/app/MessengerService.java
 *
 * Clients will register: then registered clients will receive invalidation messages, which can be
 * easily observed by the datasource on the other side.
 */
class XapiMessengerService: Service() {

    internal class IncomingHandler(
        context: Context,
        private val applicationContext: Context = context.applicationContext
    ):  Handler(Looper.getMainLooper()) {


        override fun handleMessage(msg: Message) {
            val xapiResourceProvider = applicationContext as? XapiResourceProvider
                ?: throw IllegalStateException("No xapi resource provider")

//            val endpoint = msg.data.getString(XapiIpcWhatFlags.KEY_ENDPOINT)
//                ?: throw IllegalArgumentException("Message has no endpoint")
//            val auth = msg.data.getString(XapiIpcWhatFlags.KEY_AUTH)
//                ?: throw IllegalArgumentException("Message has no auth")

            //val xapiResource = xapiResourceProvider(endpoint, auth)
            when(msg.what) {
                XapiIpcWhatFlags.WHAT_REQUEST -> {
                    //get the params and make the request
                    val reply = Message.obtain(
                        this@IncomingHandler, XapiIpcWhatFlags.WHAT_RESPONSE
                    )

                    //Mark it as a response to the request id received.
                    msg.arg1 = msg.arg1
                    msg.replyTo.send(reply)
                }

                else -> {
                    super.handleMessage(msg)
                }
            }
        }
    }

    private val messenger: Messenger by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Messenger(
            IncomingHandler(this, this.applicationContext)
        )
    }

    override fun onBind(intent: Intent): IBinder? {
        return messenger.binder
    }

    companion object {


    }

}