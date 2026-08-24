package org.openeel.libcache.ipc.server

import android.app.Application
import okhttp3.OkHttpClient

class IpcTestApplication: Application(), OkHttpClientProvider {

    private val httpClient = OkHttpClient.Builder().build()

    override fun provideOkHttpClient(): OkHttpClient {
        return httpClient
    }

}