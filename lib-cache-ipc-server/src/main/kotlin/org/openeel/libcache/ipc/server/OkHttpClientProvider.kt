package org.openeel.libcache.ipc.server
import okhttp3.OkHttpClient

interface OkHttpClientProvider {

    fun provideOkHttpClient(): OkHttpClient

}