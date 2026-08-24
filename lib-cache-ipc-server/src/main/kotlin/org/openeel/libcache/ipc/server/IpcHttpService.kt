package org.openeel.libcache.ipc.server

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import okhttp3.Call
import okhttp3.Response
import okio.IOException
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
            val replyToVal = msg.replyTo

            Log.d(HttpIpcTags.LOGTAG, "$logPrefix: request ${request.method} ${request.url}")

            val client = (this@IpcHttpService.applicationContext as? OkHttpClientProvider)
                ?.provideOkHttpClient()
                ?: throw IllegalArgumentException("Context provides no request handler")

            fun post(runnable: Runnable) = super.post(runnable)

            client.newCall(request).enqueue(
                object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {

                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.d(HttpIpcTags.LOGTAG, "$logPrefix: received response: ${response.code}")
                        val responseBundle = response.toBundle()

                        post {
                            val responseMessage = Message.obtain()

                            responseMessage.arg1 = msg.arg1
                            responseMessage.data = responseBundle
                            replyToVal.send(responseMessage)
                            Log.d(HttpIpcTags.LOGTAG, "$logPrefix: ${response.code} ${response.message}")
                        }
                    }
                }
            )
        }
    }

    private val messenger: Messenger by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Messenger(incomingHandler)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(HttpIpcTags.LOGTAG, "$logPrefix: onBind")
        return messenger.binder
    }

    override fun onDestroy() {
        //Cancel all requests

        super.onDestroy()
    }
}