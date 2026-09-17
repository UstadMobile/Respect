package org.openeel.libcache.ipc.client.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import org.openeel.libcache.ipc.client.HttpIpcClient

/**
 * OkHttp interceptor that will use the IPC response if available.
 */
@Suppress("unused") //Use as a dependency
class HttpIpcInterceptor(
    private val httpIpcClient: HttpIpcClient
): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val existingCacheControl = chain.request().header("Cache-Control")
        val directives = existingCacheControl?.split(",")?.map { it.lowercase().trim() }
            ?: emptyList()

        val ipcRequest = if("only-if-cached" in directives) {
            chain.request()
        }else {
            val newDirectives = directives + "only-if-cached"
            chain.request().newBuilder()
                .header("cache-control", newDirectives.joinToString(","))
                .build()
        }

        val ipcResponse = httpIpcClient.newCall(ipcRequest).execute()

        return if(ipcResponse.isSuccessful) {
            ipcResponse
        }else {
            chain.proceed(chain.request())
        }
    }

}