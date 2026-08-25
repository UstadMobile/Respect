package org.openeel.libcache.ipc.server

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import android.webkit.MimeTypeMap
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import okio.buffer
import okio.source
import org.openeel.libcache.ipc.core.HttpIpcTags
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class MockWebServerAssetDispatcher(
    private val context: Context,
): Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val requestPath = request.url.pathSegments.joinToString(separator = "/")

        return try {
            val input = context.assets.open(requestPath)
            val contentBytes = input.use {
                it.readAllBytes()
            }
            val contentInStream = ByteArrayInputStream(contentBytes)
            val contentSource = contentInStream.source().buffer()

            val buffer = Buffer()
            contentSource.readAll(buffer)
            val contentLength = contentBytes.size

            MockResponse.Builder()
                .body(buffer)
                .code(200)
                .addHeader("content-length", contentLength)
                .build()

        }catch (e: Throwable) {
            Log.w(HttpIpcTags.LOGTAG, "Exception retrieving ${request.url}", e)
            MockResponse.Builder()
                .body("Error retrieving $requestPath : ${e.message}")
                .code(
                    if(e is FileNotFoundException) 404 else 500
                )
                .build()
        }

    }
}