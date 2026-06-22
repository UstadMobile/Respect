package world.respect.xapi.ipc.client

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import world.respect.xapi.ipc.shared.messages.MessageData
import world.respect.xapi.ipc.shared.messages.XapiIpcTags
import world.respect.xapi.ipc.shared.messages.XapiIpcWhatFlags
import world.respect.xapi.ipc.shared.messages.ext.setFromMessageData
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * @param outgoingMessenger the outgoing messenger e.g. the one that is connected to the service binder
 */
class XapiMessageBridgeMessengerImpl(
    private val outgoingMessenger: Messenger,
): XapiMessageBridge {

    private val requestIdAtomic = AtomicInteger(1)

    private val pendingMessages = ConcurrentHashMap<Int, CompletableDeferred<MessageData>>()

    private val activeFlowChannels = ConcurrentHashMap<Int, Channel<MessageData>>()

    val incomingHandler: Handler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when {
                msg.what == XapiIpcWhatFlags.WHAT_RESPONSE -> {
                    val replyToRequestId = msg.arg1
                    val completeable = pendingMessages[replyToRequestId]
                    if(completeable != null) {
                        pendingMessages.remove(replyToRequestId)
                        val messageReply = MessageData(msg)
                        completeable.complete(messageReply)
                    }else {
                        Log.w(XapiIpcTags.LOGTAG, "XapiMessageBridgeBinderImpl: WARN: No pending message for id $replyToRequestId")
                    }
                }

                msg.what == XapiIpcWhatFlags.WHAT_FLOW_EMISSION -> {
                    val replyToRequestId = msg.arg1
                    val receiveChannel = activeFlowChannels[replyToRequestId]
                    if(receiveChannel != null) {
                        receiveChannel.trySend(MessageData(msg))
                    }else{
                        Log.w(XapiIpcTags.LOGTAG,"XapiMessageBridgeBinderImpl: WARN: No channel for id $replyToRequestId")
                    }
                }

                else -> {
                    super.handleMessage(msg)
                }
            }
        }
    }

    private val incomingMessenger: Messenger = Messenger(incomingHandler)

    override suspend fun executeForResponse(messageData: MessageData): MessageData {
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

    override fun executeForFlow(messageData: MessageData): Flow<MessageData> {
        val message = Message.obtain()
        message.setFromMessageData(messageData)
        message.replyTo = incomingMessenger
        val messageId = requestIdAtomic.getAndIncrement()
        message.arg1 = messageId

        val receiveChannel = Channel<MessageData>(capacity = Channel.BUFFERED)
        activeFlowChannels[messageId] = receiveChannel

        outgoingMessenger.send(message)

        return receiveChannel.receiveAsFlow().onCompletion {
            Log.d(XapiIpcTags.LOGTAG, "XapiMessageBridgeBinderImpl: Flow #$messageId completed")

            outgoingMessenger.send(
                Message.obtain().also {
                    it.what = XapiIpcWhatFlags.WHAT_FLOW_COMPLETION
                    it.arg1 = messageId
                }
            )

            receiveChannel.close()
        }
    }

    override fun close() {

    }
}