package org.openeel.lib.ipc.messagebridge

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
import org.openeel.lib.ipc.messagebridge.ext.setFromMessageData
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
                msg.what == IpcMessageBridgeWhatFlags.WHAT_RESPONSE -> {
                    val replyToRequestId = msg.arg1
                    val completeable = pendingMessages[replyToRequestId]
                    if(completeable != null) {
                        Log.d(
                            IpcMessageBridgeTags.LOGTAG,
                            "XapiMessageBridgeBinderImpl: receive repsonse #$replyToRequestId"
                        )
                        pendingMessages.remove(replyToRequestId)
                        val messageReply = MessageData(msg)
                        completeable.complete(messageReply)
                    }else {
                        Log.w(IpcMessageBridgeTags.LOGTAG, "XapiMessageBridgeBinderImpl: WARN: No pending message for id $replyToRequestId")
                    }
                }

                msg.what == IpcMessageBridgeWhatFlags.WHAT_FLOW_EMISSION -> {
                    val replyToRequestId = msg.arg1
                    val receiveChannel = activeFlowChannels[replyToRequestId]
                    if(receiveChannel != null) {
                        Log.d(
                            IpcMessageBridgeTags.LOGTAG,
                            "XapiMessageBridgeBinderImpl: receive flow emission for #$replyToRequestId"
                        )
                        receiveChannel.trySend(MessageData(msg))
                    }else{
                        Log.w(IpcMessageBridgeTags.LOGTAG,"XapiMessageBridgeBinderImpl: WARN: No channel for id $replyToRequestId")
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

        Log.d(IpcMessageBridgeTags.LOGTAG, "XapiMessageBridgeBinderImpl: executeForResponse: send #$messageId")
        outgoingMessenger.send(message)

        val response = completeable.await()
        Log.d(IpcMessageBridgeTags.LOGTAG, "XapiMessageBridgeBinderImpl: executeForResponse: receive response #$messageId")
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

        Log.d(IpcMessageBridgeTags.LOGTAG, "XapiMessageBridgeBinderImpl: executeForFlow: send #$messageId")
        outgoingMessenger.send(message)

        return receiveChannel.receiveAsFlow().onCompletion {
            Log.d(IpcMessageBridgeTags.LOGTAG, "XapiMessageBridgeBinderImpl: executeForFlow: Flow #$messageId completed")

            outgoingMessenger.send(
                Message.obtain().also {
                    it.what = IpcMessageBridgeWhatFlags.WHAT_FLOW_COMPLETION
                    it.arg1 = messageId
                }
            )

            receiveChannel.close()
        }
    }

    override fun close() {

    }
}