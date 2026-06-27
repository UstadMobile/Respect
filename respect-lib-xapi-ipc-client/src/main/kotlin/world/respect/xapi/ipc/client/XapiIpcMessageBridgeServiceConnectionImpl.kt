package world.respect.xapi.ipc.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import world.respect.xapi.ipc.shared.messages.MessageData
import world.respect.xapi.ipc.shared.messages.XapiIpcTags
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Binding to a service using a ServiceConnection is asynchronous. The service will normally, but
 * not always, be available when a request comes in. This wrapper takes care of waiting
 * if/when required.
 */
class XapiIpcMessageBridgeServiceConnectionImpl(
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
            Log.i(XapiIpcTags.LOGTAG, "XapiIpcMessageBridgeServiceConnectionImpl: service connected")
            mMessenger = Messenger(service).also {
                messengerBridgeFlow.value = XapiMessageBridgeMessengerImpl(it)
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i(XapiIpcTags.LOGTAG, "XapiIpcMessageBridgeServiceConnectionImpl: service disconnected")
            mMessenger = null
            messengerBridgeFlow.value = null
        }
    }

    init {
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override suspend fun executeForResponse(messageData: MessageData): MessageData {
        val currentBridgeVal = messengerBridgeFlow.value
        return currentBridgeVal?.executeForResponse(messageData)
            ?: messengerBridgeFlow.filterNotNull().first().executeForResponse(messageData)
    }

    override fun executeForFlow(messageData: MessageData): Flow<MessageData> {
        val currentBridgeVal = messengerBridgeFlow.value
        return currentBridgeVal?.executeForFlow(messageData)
            ?: flow {
                messengerBridgeFlow.filterNotNull().first().executeForFlow(messageData).collect {
                    emit(it)
                }
            }
    }

    override fun close() {
        Log.d(XapiIpcTags.LOGTAG, "XapiMessageBridgeBinderImpl: close")
        if(!closed.getAndSet(true)) {
            Log.d(XapiIpcTags.LOGTAG, "XapiMessageBridgeBinderImpl: close: cleanup")
            if(mMessenger != null) {
                Log.d(XapiIpcTags.LOGTAG, "XapiMessageBridgeBinderImpl: close: unbind")
                context.unbindService(mConnection)
                mMessenger = null
            }
        }
    }
}