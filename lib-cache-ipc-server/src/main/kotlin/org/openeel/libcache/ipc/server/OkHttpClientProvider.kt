package org.openeel.libcache.ipc.server
import okhttp3.OkHttpClient

interface OkHttpClientProvider {

    operator fun invoke(): OkHttpClient

}