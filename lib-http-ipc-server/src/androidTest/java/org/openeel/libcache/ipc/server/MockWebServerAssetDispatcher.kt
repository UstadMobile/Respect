package org.openeel.libcache.ipc.server

import android.content.Context
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest
import org.openeel.libcache.ipc.server.ext.fromAsset

class MockWebServerAssetDispatcher(
    private val context: Context,
): Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val requestPath = request.url.pathSegments.joinToString(separator = "/")

        return MockResponse.Builder()
            .fromAsset(requestPath, context)
            .build()
    }
}