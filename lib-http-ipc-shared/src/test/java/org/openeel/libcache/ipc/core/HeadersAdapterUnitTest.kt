package org.openeel.libcache.ipc.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.Headers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.openeel.libcache.ipc.core.adapters.toBundle
import org.openeel.libcache.ipc.core.adapters.toHeaders

@RunWith(AndroidJUnit4::class)
class HeadersAdapterUnitTest {

    @Test
    fun whenConvertedToFromBundleThenMatches() {
        val headers = Headers.Builder()
            .add("content-type", "application/json")
            .build()

        val bundle = headers.toBundle()
        val headersFromBundle = bundle.toHeaders()

        Assert.assertEquals(headers, headersFromBundle)

    }

}