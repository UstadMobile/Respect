package org.openeel.libcache.ipc.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This might look like a JVM unit test would be fine, however Robolectric's ParcelFileDescriptor
 * implementation is much more lenient than the real one leading to tests that pass on Robolectric
 * which fail on Android itself
 */
@RunWith(AndroidJUnit4::class)
class ResponseAdapterTest {

    @Test
    fun givenConvertedToFromBundleThenShouldMatch() {
        val request = Request.Builder()
            .url("http://localhost:8098/")
            .build()

        val message = "Gateway Timeout: only-if-cached if true, but not available in cache"
        val contentType = "text/plain"
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .message("Gateway Timeout")
            .header("content-type", contentType)
            .code(504)
            .body(message.toResponseBody(contentType = contentType.toMediaTypeOrNull()))
            .build()

        val responseBundle = response.toBundle()
        val responseFromBundle = responseBundle.toResponse(request)

        val responseFromBundleBody = responseFromBundle.body.string()
        Assert.assertEquals(message,responseFromBundleBody)
        Assert.assertEquals(response.headers, responseFromBundle.headers)
        Assert.assertEquals(response.code, responseFromBundle.code)
        Assert.assertEquals(response.protocol, responseFromBundle.protocol)
    }

}