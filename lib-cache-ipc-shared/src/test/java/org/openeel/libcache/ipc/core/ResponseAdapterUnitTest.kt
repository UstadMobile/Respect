package org.openeel.libcache.ipc.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResponseAdapterUnitTest {

    @Test
    fun givenConvertedToFromBundleThenShouldMatch() {
        val request = Request.Builder()
            .url("http://localhost:8098/")
            .build()

        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .message("Gateway Timeout")
            .code(504)
            .body("Gateway Timeout: only-if-cached if true, but not available in cache".toResponseBody())
            .build()

        val responseBundle = response.toBundle()
        val responseFromBundle = responseBundle.toResponse(request)

        Assert.assertEquals(
            response.body.string(),
            responseFromBundle.body.string()
        )
        Assert.assertEquals(response.code, responseFromBundle.code)
        Assert.assertEquals(response.protocol, responseFromBundle.protocol)
    }

}