package org.openeel.libcache.ipc.server

import android.app.Application
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class IpcTestApplication: Application(), OkHttpClientProvider {

    private val httpClient = OkHttpClient.Builder()
        .callTimeout(TIMEOUT_DURATION_SECS, TimeUnit.SECONDS)
        .build()

    override fun provideOkHttpClient(): OkHttpClient {
        return httpClient
    }

    companion object {

        val TIMEOUT_DURATION_SECS = 2L

    }
}