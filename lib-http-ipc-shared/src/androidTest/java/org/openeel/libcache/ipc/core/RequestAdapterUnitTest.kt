package org.openeel.libcache.ipc.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.buffer
import okio.sink
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.openeel.libcache.ipc.core.adapters.toBundle
import org.openeel.libcache.ipc.core.adapters.toRequest
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)

class RequestAdapterUnitTest {

    private lateinit var executorService: ExecutorService

    @Before
    fun setup() {
        executorService = Executors.newCachedThreadPool()
    }

    @After
    fun tearDown() {
        executorService.shutdown()
    }


    @Test
    fun givenRequest_whenConverted_thenShouldMatch() {
        val postBodyContent = "HELLO WORLD"

        val request = Request.Builder()
            .url("http://localhost:8098/")
            .post(postBodyContent.toRequestBody("text/plain".toMediaType()))
            .build()
        val requestBundle = request.toBundle(executorService)

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