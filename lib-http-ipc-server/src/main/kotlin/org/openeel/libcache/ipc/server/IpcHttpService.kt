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
import org.openeel.libcache.ipc.server.ext.setErrorResponse

class IpcHttpService: Service() {

    private val logPrefix = "IpcHttpService"

    private val handlerThread = HandlerThread("HttpIpcServiceThread").also {
        if(!it.isAlive)
            it.start()
    }

    val incomingHandler = object: Handler(handlerThread.looper) {

        override fun handleMessage(msg: Message) {
            val request = msg.data.toRequest()
            val replyToVal = msg.replyTo
            val callIdVal = msg.arg1
            val methodAndUrlStr = "${request.method} ${request.url}"

            val client = (this@IpcHttpService.applicationContext as? OkHttpClientProvider)
                ?.provideOkHttpClient()
                ?: throw IllegalArgumentException("Context provides no request handler")

            fun post(runnable: Runnable) = super.post(runnable)

            client.newCall(request).enqueue(
                object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.w(HttpIpcTags.LOGTAG, "ERROR: $logPrefix $methodAndUrlStr : onFailure", e)
                        val responseBundle = Response.Builder().setErrorResponse(
                            request = request,
                            exception = e,
                        ).build().toBundle(client.dispatcher.executorService)

                        post {
                            replyToVal.send(
                                Message.obtain().also {
                                    it.arg1 = callIdVal
                                    it.data = responseBundle
                                }
                            )
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBundle = response.toBundle(
                            client.dispatcher.executorService
                        )

                        post {
                            Log.d(HttpIpcTags.LOGTAG, "$logPrefix:${response.code} ${response.message} $methodAndUrlStr ")
                            replyToVal.send(
                                Message.obtain().also {
                                    it.arg1 = callIdVal
                                    it.data = responseBundle
                                }
                            )
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
        Log.i(HttpIpcTags.LOGTAG, "$logPrefix: onDestroy")

        // Remove all pending messages as per
        // https://developer.android.com/reference/android/os/Handler#removeCallbacksAndMessages(java.lang.Object)
        incomingHandler.removeCallbacksAndMessages(null)

        handlerThread.quitSafely()
        super.onDestroy()
    }

}