package org.openeel.lib.ipc.messagebridge

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Binding to a service using a ServiceConnection is asynchronous. The service will normally, but
 * not always, be available when a request comes in. This wrapper takes care of waiting
 * if/when required.
 */
class IpcMessageBridgeServiceConnectionImpl(
    private val context: Context,
    private val intent: Intent,
) : XapiMessageBridge {

    private var mMessenger: Messenger? = null

    private val messengerBridgeFlow = MutableStateFlow<XapiMessageBridgeMessengerImpl?>(null)

    private val closed = AtomicBoolean(false)

    private val mConnection = object: ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName,
            service: IBinder
        ) {
            Log.i(IpcMessageBridgeTags.LOGTAG, "IpcMessageBridgeServiceConnectionImpl: service connected: $name")
            mMessenger = Messenger(service).also {
                messengerBridgeFlow.value = XapiMessageBridgeMessengerImpl(it)
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i(IpcMessageBridgeTags.LOGTAG, "IpcMessageBridgeServiceConnectionImpl: service disconnected: $name")
            mMessenger = null
            messengerBridgeFlow.value = null
        }
    }

    init {
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override suspend fun executeForResponse(messageData: MessageData): MessageData {
        val currentBridgeVal = messengerBridgeFlow.value
        val logPrefix = "IpcMessageBridgeServiceConnectionImpl: executeForResponse server=${intent.`package`} "
        return currentBridgeVal?.also {
            Log.d(IpcMessageBridgeTags.LOGTAG, "$logPrefix send direct")
        }?.executeForResponse(messageData)
            ?: messengerBridgeFlow.also {
                Log.d(IpcMessageBridgeTags.LOGTAG, "$logPrefix queue to send")
            }.filterNotNull().first().executeForResponse(messageData)
    }

    override fun executeForFlow(messageData: MessageData): Flow<MessageData> {
        val currentBridgeVal = messengerBridgeFlow.value
        val logPrefix = "IpcMessageBridgeServiceConnectionImpl: executeForFlow server=${intent.`package`} "
        return currentBridgeVal?.also {
            Log.d(IpcMessageBridgeTags.LOGTAG, "$logPrefix send direct")
        }?.executeForFlow(messageData)
            ?: flow {
                Log.d(IpcMessageBridgeTags.LOGTAG, "$logPrefix queue to send")
                messengerBridgeFlow.filterNotNull().first().executeForFlow(messageData).collect {
                    emit(it)
                }
            }
    }

    override fun close() {
        Log.d(IpcMessageBridgeTags.LOGTAG, "IpcMessageBridgeServiceConnectionImpl: close")
        if(!closed.getAndSet(true)) {
            Log.d(IpcMessageBridgeTags.LOGTAG, "IpcMessageBridgeServiceConnectionImpl: close: cleanup")
            if(mMessenger != null) {
                Log.d(IpcMessageBridgeTags.LOGTAG, "IpcMessageBridgeServiceConnectionImpl: close: unbind")
                context.unbindService(mConnection)
                mMessenger = null
            }
        }
    }
}