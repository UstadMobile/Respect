package org.openeel.libcache.ipc.client

import okhttp3.Call
import okhttp3.Request
import java.io.Closeable

interface HttpIpcClient: Closeable {

    fun newCall(request: Request): Call

}