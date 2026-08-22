package org.openeel.libcache.ipc.server

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import org.openeel.libcache.ipc.core.HttpIpcTags
import org.openeel.libcache.ipc.core.toBundle
import org.openeel.libcache.ipc.core.toRequest

class IpcHttpService: Service() {

    private val logPrefix = "IpcHttpService"

    private val handlerThread = HandlerThread("HttpIpcServiceThread").also {
        if(!it.isAlive)
            it.start()
    }

    val incomingHandler = object: Handler(handlerThread.looper) {
        override fun handleMessage(msg: Message) {
            Log.d(HttpIpcTags.LOGTAG, "$logPrefix: Received message")
            val request = msg.data.toRequest()
            Log.d(HttpIpcTags.LOGTAG, "$logPrefix: request ${request.method} ${request.url}")

            val client = (this@IpcHttpService.applicationContext as? OkHttpClientProvider)
                ?.provideOkHttpClient()
                ?: throw IllegalArgumentException("Context provides no request handler")

            val response = client.newCall(request).execute()
            Log.d(HttpIpcTags.LOGTAG, "$logPrefix: received response: ${response.code}")

            val responseMessage = Message.obtain()

            responseMessage.arg1 = msg.arg1
            responseMessage.data = response.toBundle()
            msg.replyTo.send(responseMessage)
            Log.d(HttpIpcTags.LOGTAG, "$logPrefix: sent response message: ${response.code}")
        }
    }

    private val messenger: Messenger by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Messenger(incomingHandler)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(HttpIpcTags.LOGTAG, "$logPrefix: onBind")
        return messenger.binder
    }
}