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
import org.openeel.libcache.ipc.core.toBundle
import org.openeel.libcache.ipc.core.toResponse
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass

@OptIn(ExperimentalAtomicApi::class)
class IpcHttpCall(
    internal val callId: Int,
    private val request: Request,
    private val executorService: ExecutorService,
    private val getMessenger: () -> IpcHttpClientImpl.Messengers,
): Call {

    private val logPrefix: String = "Call #$callId ${request.method} ${request.url}"

    private val executorFuture = AtomicReference<Future<*>?>(null)

    private val completeableFuture = CompletableFuture<Response>()

    internal fun onResponse(message: Message) {
        completeableFuture.complete(message.data.toResponse(request))
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
        Log.d(HttpIpcTags.LOGTAG, "$logPrefix: enqueue")
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

        val message = Message.obtain()
        message.replyTo = messengers.incoming
        message.data = request.toBundle()
        message.arg1 = callId
        messengers.outgoing.send(message)

        Log.d(HttpIpcTags.LOGTAG, "$logPrefix: sent message")

        return completeableFuture.join().also {
            Log.d(HttpIpcTags.LOGTAG, "$logPrefix: received response message")
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
        TODO("Not yet implemented")
    }
}