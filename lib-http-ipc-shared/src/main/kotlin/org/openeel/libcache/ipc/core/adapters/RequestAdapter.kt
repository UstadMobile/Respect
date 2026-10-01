package org.openeel.libcache.ipc.core.adapters

import android.os.Bundle
import android.os.ParcelFileDescriptor
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.buffer
import okio.sink
import org.openeel.libcache.ipc.core.HttpIpcKeys
import java.util.concurrent.ExecutorService

/**
 * Convert an OKHttp request into an Android bundle that can be passed over a bound service using
 * a Messenger.
 *
 * If there is a request body, it will be sent as a ParcelFileDescriptor
 *
 * @receiver The OKHttp request to convert.
 */
fun Request.toBundle(
    executor: ExecutorService
): Bundle {
    val bundle = Bundle()

    bundle.putBundle(HttpIpcKeys.KEY_HEADERS, headers.toBundle())
    bundle.putString(HttpIpcKeys.KEY_URL, url.toString())
    bundle.putString(HttpIpcKeys.KEY_METHOD, method)

    val bodyVal = body
    if(bodyVal != null) {
        val  pipe = ParcelFileDescriptor.createPipe()

        //As per https://developer.android.com/reference/android/os/ParcelFileDescriptor#createPipe()
        val readSide = pipe[0]
        val writeSide = pipe[1]

        executor.submit {
            ParcelFileDescriptor.AutoCloseOutputStream(writeSide).use { out ->
                val sink = out.sink().buffer()
                bodyVal.writeTo(sink)
                sink.flush()
            }
        }

        bundle.putParcelable(HttpIpcKeys.KEY_BODY_FD, writeSide)
    }

    return bundle
}

/**
 * Convert a bundle that was created using Request.toBundle back into an OKHttp request.
 *
 * @receiver The bundle to convert that was created using Request.toBundle
 *
 */
fun Bundle.toRequest() : Request {
    val builder = Request.Builder()

    val url = getString(HttpIpcKeys.KEY_URL)
        ?: throw IllegalArgumentException("Missing URL in Bundle")
    val method = getString(HttpIpcKeys.KEY_METHOD)
        ?: throw IllegalArgumentException("Missing Method in Bundle")
    val headerBundle = getBundle(HttpIpcKeys.KEY_HEADERS)

    builder.url(url)

    val headers = headerBundle?.toHeaders()?.also {
        builder.headers(it)
    } ?: Headers.EMPTY


    var requestBody: RequestBody? = null
    val fd = getParcelable<ParcelFileDescriptor>(HttpIpcKeys.KEY_BODY_FD)

    if (fd != null) {
        requestBody = fd.fileDescriptor.toRequestBody(headers.get("content-type")
            ?.toMediaTypeOrNull())
    }

    builder.method(method, requestBody)

    return builder.build()
}
