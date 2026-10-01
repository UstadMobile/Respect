package org.openeel.libcache.ipc.core.adapters

import android.os.Bundle
import android.os.ParcelFileDescriptor
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.internal.http.promisesBody
import okio.buffer
import okio.source
import okio.sink
import org.openeel.libcache.ipc.core.HttpIpcKeys
import java.util.concurrent.ExecutorService

fun Response.toBundle(
    executor: ExecutorService
): Bundle {
    val bundle = Bundle()
    bundle.putInt(HttpIpcKeys.KEY_STATUS_CODE, code)
    bundle.putString(HttpIpcKeys.KEY_STATUS_MESSAGE, message)
    bundle.putBundle(HttpIpcKeys.KEY_HEADERS, headers.toBundle())
    bundle.putString(HttpIpcKeys.KEY_RESPONSE_PROTOCOL, protocol.name)

    if(promisesBody()) {
        val pipe = ParcelFileDescriptor.createPipe()

        //As per https://developer.android.com/reference/android/os/ParcelFileDescriptor#createPipe()
        val readSide = pipe[0]
        val writeSide = pipe[1]

        bundle.putParcelable(HttpIpcKeys.KEY_BODY_FD, readSide)

        executor.submit {
            ParcelFileDescriptor.AutoCloseOutputStream(writeSide).use { parcelOut ->
                body.source().use { source ->
                    val sink = parcelOut.sink().buffer()
                    source.readAll(sink)
                    sink.flush()
                }
            }
        }

    }

    return bundle
}

fun Bundle.toResponse(
    request: Request,
) : Response {
    val bodyFd = getParcelable<ParcelFileDescriptor>(HttpIpcKeys.KEY_BODY_FD)
    val headers = getBundle(HttpIpcKeys.KEY_HEADERS)?.toHeaders() ?: Headers.EMPTY
    val protocolName = getString(HttpIpcKeys.KEY_RESPONSE_PROTOCOL)
        ?: throw IllegalArgumentException("Missing protocol name")

    val responseBody = bodyFd?.let { fd ->
        val source = ParcelFileDescriptor.AutoCloseInputStream(fd).source().buffer()
        val contentType = headers["Content-Type"]
        val contentLength = headers["Content-Length"]?.toLongOrNull() ?: -1L
        source.asResponseBody(contentType?.toMediaTypeOrNull(), contentLength)
    }

    return Response.Builder()
        .headers(headers)
        .request(request)
        .code(getInt(HttpIpcKeys.KEY_STATUS_CODE))
        .message(
            getString(HttpIpcKeys.KEY_STATUS_MESSAGE)
                ?: throw IllegalArgumentException("Missing status message")
        )
        .protocol(Protocol.entries.first { it.name == protocolName })
        .apply {
            if(responseBody != null) {
                body(responseBody)
            }
        }
        .build()
}
