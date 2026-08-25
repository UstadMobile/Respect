package org.openeel.libcache.ipc.client

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.Messenger
import okhttp3.Call
import okhttp3.Request
import org.openeel.lib.ipc.messagebridge.MessengerProvider
import java.io.Closeable
import java.io.IOException
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class IpcHttpClientImpl(
    private val outgoingMessengerProvider: MessengerProvider,
) : IpcHttpClient, Closeable {

    private val handlerThread = HandlerThread("HttpIpcServiceThread").also {
        if(!it.isAlive)
            it.start()
    }

    data class Messengers(
        val incoming: Messenger,
        val outgoing: Messenger,
    )

    private val executor = Executors.newCachedThreadPool()

    private val callIdAtomic = AtomicInteger()

    private val closed = AtomicBoolean()

    private val calls = Collections.synchronizedSet(
        Collections.newSetFromMap(
            WeakHashMap<IpcHttpCall, Boolean>()
        )
    )

    val incomingHandler: Handler = object: Handler(handlerThread.looper) {

        override fun handleMessage(msg: Message) {
            val callId = msg.arg1
            calls.firstOrNull { it.callId == callId }?.onResponse(msg)
        }

    }

    private val incomingMessenger: Messenger = Messenger(incomingHandler)

    internal fun assertNotClosed() {
        if(closed.get())
            throw IOException("IpcHttpClient is closed")
    }

    override fun newCall(request: Request): Call {
        assertNotClosed()
        val callId = callIdAtomic.getAndIncrement()

        return IpcHttpCall(
            callId = callId,
            request = request,
            executorService = executor,
            getMessenger = {
                Messengers(incoming = incomingMessenger, outgoing = outgoingMessengerProvider())
            },
        ).also {
            calls.add(it)
        }
    }


    override fun close() {
        if(!closed.getAndSet(true)) {
            //As per https://developer.android.com/reference/android/os/Handler#removeCallbacksAndMessages(java.lang.Object)
            incomingHandler.removeCallbacksAndMessages(null)

            handlerThread.quitSafely()
        }
    }

}