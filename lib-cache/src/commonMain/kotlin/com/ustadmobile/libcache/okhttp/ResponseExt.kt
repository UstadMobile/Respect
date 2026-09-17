package com.ustadmobile.libcache.okhttp

import okhttp3.Response
import okhttp3.internal.toLongOrDefault

fun Response.headersContentLength(): Long {
    return headers["Content-Length"]?.toLongOrDefault(-1L) ?: -1L
}
