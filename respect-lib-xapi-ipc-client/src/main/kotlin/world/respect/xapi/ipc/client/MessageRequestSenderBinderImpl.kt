package world.respect.xapi.ipc.client

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import kotlinx.coroutines.CompletableDeferred
import world.respect.xapi.ipc.shared.messages.MessageData
import world.respect.xapi.ipc.shared.messages.XapiIpcWhatFlags
import world.respect.xapi.ipc.shared.messages.ext.setFromMessageData
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * @param outgoingMessenger the outgoing messenger e.g. the one that is connected to the service binder
 */
class MessageRequestSenderBinderImpl(
    private val outgoingMessenger: Messenger,
): MessageRequestSender {

    private val requestIdAtomic = AtomicInteger(1)

    private val pendingMessages = ConcurrentHashMap<Int, CompletableDeferred<MessageData>>()

    val incomingHandler: Handler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when {
                msg.what == XapiIpcWhatFlags.WHAT_RESPONSE -> {
                    val replyToRequestId = msg.arg1
                    val completeable = pendingMessages[replyToRequestId]
                    if(completeable != null) {
                        pendingMessages.remove(replyToRequestId)
                        val messageReply = MessageData(
                            data = Bundle(msg.data),
                            what = msg.what,
                            arg1 = msg.arg1,
                            arg2 = msg.arg2,
                        )
                        completeable.complete(messageReply)
                    }else {
                        println("WARN: No pending message for id $replyToRequestId")
                    }
                }

                else -> {
                    super.handleMessage(msg)
                }
            }
        }
    }

    private val incomingMessenger: Messenger = Messenger(incomingHandler)

    override suspend fun executeRequest(messageData: MessageData): MessageData {
        val message = Message.obtain()
        message.setFromMessageData(messageData)
        message.replyTo = incomingMessenger
        val messageId = requestIdAtomic.getAndIncrement()
        message.arg1 = messageId

        val completeable = CompletableDeferred<MessageData>().also {
            pendingMessages[messageId] = it
        }

        outgoingMessenger.send(message)

        val response = completeable.await()
        return response
    }

}