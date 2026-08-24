package org.openeel.libcache.ipc.client

import okhttp3.Call
import okhttp3.Request

interface IpcHttpClient {

    fun newCall(request: Request): Call

}