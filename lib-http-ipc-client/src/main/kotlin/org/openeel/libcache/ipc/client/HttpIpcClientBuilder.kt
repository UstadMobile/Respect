package org.openeel.libcache.ipc.client

import android.content.Context
import android.content.Intent
import org.openeel.lib.ipc.messagebridge.ServiceConnectionMessengerProvider
import org.openeel.libcache.ipc.core.HttpIpcIntent

class HttpIpcClientBuilder(
    private val context: Context,
){

    private var ipcPackageServiceName: String? = null

    private var auth: String? = null

    fun setIpcServicePackageName(
        ipcPackageServiceName: String
    ): HttpIpcClientBuilder {
        this.ipcPackageServiceName = ipcPackageServiceName
        return this
    }

    fun setAuth(auth: String): HttpIpcClientBuilder {
        this.auth = auth
        return this
    }

    fun build(): HttpIpcClient {
        return HttpIpcClientImpl(
            outgoingMessengerProvider = ServiceConnectionMessengerProvider(
                context = context,
                intent = Intent(HttpIpcIntent.ACTION_HTTP_OVER_IPC_CONNECT).also {
                    it.`package` = ipcPackageServiceName
                }
            )
        )
    }

}