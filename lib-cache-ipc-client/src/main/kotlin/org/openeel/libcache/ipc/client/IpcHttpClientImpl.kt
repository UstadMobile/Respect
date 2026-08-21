package org.openeel.libcache.ipc.client

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import okhttp3.Call
import okhttp3.Request
import org.openeel.lib.ipc.messagebridge.MessengerProvider
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class IpcHttpClientImpl(
    private val outgoingMessengerProvider: MessengerProvider,
) : IpcHttpClient {

    data class Messengers(
        val incoming: Messenger,
        val outgoing: Messenger,
    )

    private val executor = Executors.newCachedThreadPool()

    private val callIdAtomic = AtomicInteger()

    val incomingHandler: Handler = object: Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            val callId = msg.arg1
            calls[callId]?.onResponse(msg)
        }

    }

    private val incomingMessenger: Messenger = Messenger(incomingHandler)

    private val calls = ConcurrentHashMap<Int, IpcHttpCall>()

    override fun newCall(request: Request): Call {
        val callId = callIdAtomic.getAndIncrement()
        return IpcHttpCall(
            callId = callId,
            request = request,
            executorService = executor,
            getMessenger = {
                Messengers(incoming = incomingMessenger, outgoing = outgoingMessengerProvider())
            },
        ).also {
            calls[callId] = it
        }
    }

}