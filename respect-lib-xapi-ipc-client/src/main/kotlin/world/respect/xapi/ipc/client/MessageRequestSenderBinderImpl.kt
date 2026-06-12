package world.respect.xapi.ipc.client

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import kotlinx.coroutines.CompletableDeferred
import world.respect.xapi.ipc.shared.messages.XapiIpcWhatFlags
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * @param outgoingMessenger the outgoing messenger e.g. the one that is connected to the service binder
 */
class MessageRequestSenderBinderImpl(
    private val outgoingMessenger: Messenger,
): MessageRequestSender {

    private val requestIdAtomic = AtomicInteger()

    private val pendingMessages = ConcurrentHashMap<Int, CompletableDeferred<Message>>()

    val incomingHandler: Handler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when {
                msg.what == XapiIpcWhatFlags.WHAT_RESPONSE -> {
                    val replyToRequestId = msg.arg1
                    val completeable = pendingMessages[replyToRequestId]
                    if(completeable != null) {
                        pendingMessages.remove(replyToRequestId)
                        completeable.complete(msg)
                    }else {
                        println("WARN: No pending message for id $replyToRequestId")
                    }
                }
            }


            super.handleMessage(msg)
        }
    }

    private val incomingMessenger: Messenger = Messenger(incomingHandler)

    override suspend fun sendRequest(message: Message): Message {
        message.replyTo = incomingMessenger
        message.what = XapiIpcWhatFlags.WHAT_REQUEST
        val messageId = requestIdAtomic.getAndIncrement()

        message.arg1 = messageId
        val completeable = CompletableDeferred<Message>().also {
            pendingMessages[messageId] = it
        }

        outgoingMessenger.send(message)

        val response = completeable.await()
        return response
    }

}