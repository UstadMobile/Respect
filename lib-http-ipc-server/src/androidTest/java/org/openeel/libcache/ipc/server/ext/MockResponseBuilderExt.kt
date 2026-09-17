package org.openeel.libcache.ipc.server.ext

import android.content.Context
import android.util.Log
import mockwebserver3.MockResponse
import okio.Buffer
import okio.buffer
import okio.source
import org.openeel.libcache.ipc.core.HttpIpcTags
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException

fun MockResponse.Builder.fromAsset(
    assetPath: String,
    context: Context
): MockResponse.Builder {

    try {
        val input = context.assets.open(assetPath)
        val contentBytes = input.use {
            it.readAllBytes()
        }
        val contentInStream = ByteArrayInputStream(contentBytes)
        val contentSource = contentInStream.source().buffer()

        val buffer = Buffer()
        contentSource.readAll(buffer)
        val contentLength = contentBytes.size


        return this.body(buffer)
            .code(200)
            .addHeader("content-length", contentLength)

    }catch (e: Throwable) {
        Log.w(HttpIpcTags.LOGTAG, "Exception retrieving ${assetPath}", e)
        return this.body("Error retrieving $assetPath : ${e.message}")
            .code(
                if(e is FileNotFoundException) 404 else 500
            )
    }

}