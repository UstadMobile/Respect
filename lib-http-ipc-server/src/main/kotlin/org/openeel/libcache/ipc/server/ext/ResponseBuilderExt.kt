package org.openeel.libcache.ipc.server.ext

import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

fun Response.Builder.setErrorResponse(
    request: Request,
    exception: Throwable,
    code: Int = 502,
    message: String = "Bad Gateway",
): Response.Builder {
    return request(request)
        .protocol(Protocol.HTTP_1_1)
        .message(message)
        .code(code)
        .body("Error handling request: ${exception.message}".toResponseBody())
}