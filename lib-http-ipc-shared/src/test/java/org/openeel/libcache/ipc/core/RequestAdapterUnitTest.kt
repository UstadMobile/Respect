package org.openeel.libcache.ipc.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.io.ByteArrayDataOutput
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.buffer
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import okio.sink

@RunWith(AndroidJUnit4::class)

class RequestAdapterUnitTest {

    @Test
    fun givenRequest_whenConverted_thenShouldMatch() {
        val postBodyContent = "HELLO WORLD"

        val request = Request.Builder()
            .url("http://localhost:8098/")
            .post(postBodyContent.toRequestBody("text/plain".toMediaType()))
            .build()
        val requestBundle = request.toBundle()

        val requestFromBundle = requestBundle.toRequest()
        Assert.assertEquals(request.url, requestFromBundle.url)
        Assert.assertEquals(request.headers, requestFromBundle.headers)
        Assert.assertEquals(request.method, requestFromBundle.method)

        val byteArrayOutput = ByteArrayOutputStream()
        val sink = byteArrayOutput.sink().buffer()
        request.body!!.writeTo(sink)
        sink.flush()

        val byteArray = byteArrayOutput.toByteArray()

        val bodyStrFromBundle = byteArray.decodeToString()
        Assert.assertEquals(postBodyContent, bodyStrFromBundle)
    }

}