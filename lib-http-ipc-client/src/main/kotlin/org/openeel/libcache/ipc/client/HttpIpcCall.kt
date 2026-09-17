package org.openeel.libcache.ipc.client

import android.os.Message
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.EventListener
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import okio.Timeout
import org.openeel.libcache.ipc.core.HttpIpcTags
import org.openeel.libcache.ipc.core.HttpIpcWhat
import org.openeel.libcache.ipc.core.adapters.toBundle
import org.openeel.libcache.ipc.core.adapters.toResponse
import org.openeel.libcache.ipc.core.adapters.toTimeout
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass

@OptIn(ExperimentalAtomicApi::class)
class HttpIpcCall(
    internal val callId: Int,
    private val request: Request,
    private val executorService: ExecutorService,
    private val getMessenger: () -> HttpIpcClientImpl.Messengers,
): Call {

    private val logPrefix: String = "HttpIpcCall #$callId ${request.method} ${request.url}"

    private val executorFuture = AtomicReference<Future<*>?>(null)

    private val completeableFuture = CompletableFuture<Response>()

    private val timeoutFuture by lazy {
        CompletableFuture<Timeout>()
    }

    internal fun onMessage(message: Message) {
        when(message.what) {
            HttpIpcWhat.WHAT_RESPONSE -> {
                completeableFuture.complete(message.data.toResponse(request))
            }

            HttpIpcWhat.WHAT_RESPONSE_TIMEOUT -> {
                timeoutFuture.complete(message.data.toTimeout())
            }
        }

    }

    override fun addEventListener(eventListener: EventListener) {

    }

    override fun cancel() {
        Log.d(HttpIpcTags.LOGTAG, "$logPrefix: cancel")
        executorFuture.load()?.cancel(true)
        completeableFuture.cancel(true)
    }

    override fun clone(): Call {
        TODO()
        //return IpcHttpCall(42, request,)
    }

    override fun enqueue(responseCallback: Callback) {
        executorService.submit {
            try {
                val response = execute()
                responseCallback.onResponse(this, response)
            }catch(e: Throwable) {
                responseCallback.onFailure(
                    call = this,
                    e = e as? IOException ?: IOException(e)
                )
            }
        }.also {
            executorFuture.store(it)
        }
    }

    override fun execute(): Response {
        //send on the messenger.
        val messengers = getMessenger()

        return try {
            val message = Message.obtain()
            message.what = HttpIpcWhat.WHAT_REQUEST
            message.replyTo = messengers.incoming
            message.data = request.toBundle(executor = executorService)
            message.arg1 = callId
            messengers.outgoing.send(message)
            Log.d(HttpIpcTags.LOGTAG, "HttpIpcCall: #$callId: ${request.method} ${request.url} : sent via IPC messenger")


            completeableFuture.join().also {
                Log.d(HttpIpcTags.LOGTAG,
                    "HttpIpcCall: #$callId Response: ${it.code} ${it.message} ${it.request.url}"
                )
            }
        }catch(e: Throwable) {
            Log.w(HttpIpcTags.LOGTAG, "HttpIpcCall #$callId: ${request.method} ${request.url} Exception", e)
            throw e
        }
    }

    override fun isCanceled(): Boolean {
        return completeableFuture.isCancelled
    }

    override fun isExecuted(): Boolean {
        return completeableFuture.isDone
    }

    override fun request(): Request {
        return request()
    }

    override fun <T : Any> tag(type: KClass<T>): T? {
        return type.java.cast(request.tag(type))
    }

    override fun <T> tag(type: Class<out T>): T? {
        return type.cast(request.tag(type))
    }

    override fun <T : Any> tag(type: KClass<T>, computeIfAbsent: () -> T): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any> tag(type: Class<T>, computeIfAbsent: () -> T): T {
        TODO("Not yet implemented")
    }

    override fun timeout(): Timeout {
        if(timeoutFuture.isDone) {
            return timeoutFuture.get()
        }

        val messengers = getMessenger()
        messengers.outgoing.send(
            Message.obtain().also {
                it.what = HttpIpcWhat.WHAT_REQUEST_TIMEOUT
                it.arg1 = callId
                it.replyTo = messengers.incoming
            }
        )

        return timeoutFuture.join()
    }
}