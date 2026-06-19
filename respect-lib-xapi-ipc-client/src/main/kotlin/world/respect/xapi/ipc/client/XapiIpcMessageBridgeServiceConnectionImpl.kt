package world.respect.xapi.ipc.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Messenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import world.respect.xapi.ipc.shared.messages.MessageData

/**
 * Binding to a service using a ServiceConnection is asynchronous. The service will normally, but
 * not always, be available when a request comes in. This wrapper takes care of waiting
 * if/when required.
 */
class XapiIpcMessageBridgeServiceConnectionImpl(
    private val context: Context
) : XapiMessageBridge {

    private var mMessenger: Messenger? = null

    private val messengerBridgeFlow = MutableStateFlow<XapiMessageBridgeMessengerImpl?>(null)

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val mConnection = object: ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName,
            service: IBinder
        ) {
            mMessenger = Messenger(service).also {
                messengerBridgeFlow.value = XapiMessageBridgeMessengerImpl(it)
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mMessenger = null
            messengerBridgeFlow.value = null
        }
    }

    init {
        //do the binding here

        val intent = Intent()
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

    fun close() {

        //run disconnect here.

    }
}