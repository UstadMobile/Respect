package org.openeel.libcache.ipc.server

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import org.openeel.libcache.ipc.core.toBundle
import org.openeel.libcache.ipc.core.toRequest

class IpcHttpService: Service() {

    private val handlerThread = HandlerThread("HttpIpcServiceThread").also {
        if(!it.isAlive)
            it.start()
    }

    val incomingHandler = object: Handler(handlerThread.looper) {
        override fun handleMessage(msg: Message) {
            val request = msg.data.toRequest()
            val client = (this@IpcHttpService.applicationContext as? OkHttpClientProvider)?.invoke()
                ?: throw IllegalArgumentException("Context provides no request handler")

            val response = client.newCall(request).execute()

            val responseMessage = Message.obtain()

            responseMessage.arg1 = msg.arg1
            responseMessage.data = response.toBundle()
            msg.replyTo.send(responseMessage)
        }
    }

    private val messenger: Messenger by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Messenger(incomingHandler)
    }

    override fun onBind(intent: Intent): IBinder? {
        return messenger.binder
    }
}