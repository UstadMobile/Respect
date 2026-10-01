package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.ihttp.headers.iHeadersBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class CacheControlFreshnessCheckerImplTest {

    @Test
    fun givenResponseWithinMaxAge_whenChecked_isFresh() {
        val responseHeaders = iHeadersBuilder {
            header("cache-control", "max-age=86400")
        }
        val requestHeaders = iHeadersBuilder {
            header("cache-control", "cache")
        }

        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders = requestHeaders,
            responseHeaders = responseHeaders,
            responseLastValidated = Clock.System.now().toEpochMilliseconds() - 60000,
            responseFirstStoredTime = Clock.System.now().toEpochMilliseconds() - 60000
        )
        assertTrue(status.isFresh, "Resource within maxage is fresh")
    }

    @Test
    fun givenResponseHasMustRevalidate_whenChecked_isStale() {
        val etag = "tagged"
        val lastModified = "Tue, 22 Feb 2022 20:20:20 GMT"
        val responseHeaders = iHeadersBuilder {
            header("cache-control", "must-revalidate")
            header("etag", etag)
            header("last-modified", lastModified)
        }
        val requestHeaders = iHeadersBuilder {

        }

        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders = requestHeaders,
            responseHeaders = responseHeaders,
            responseLastValidated = Clock.System.now().toEpochMilliseconds(),
            responseFirstStoredTime = Clock.System.now().toEpochMilliseconds()
        )

        assertFalse(status.isFresh)
        assertEquals(status.ifNoneMatch, etag)
        assertEquals(status.ifNotModifiedSince, lastModified)
    }

    @Test
    fun givenResponseIsImmutable_whenChecked_isFresh() {
        val responseHeaders = iHeadersBuilder {
            header("cache-control", "immutable")
        }
        val requestHeaders = iHeadersBuilder {  }

        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders = requestHeaders,
            responseHeaders = responseHeaders,
            responseLastValidated = Clock.System.now().toEpochMilliseconds() - 60000,
            responseFirstStoredTime = Clock.System.now().toEpochMilliseconds() - 60000
        )

        assertTrue(status.isFresh)
    }

    @Test
    fun givenRequestHasNoCacheDirective_whenChecked_isStale() {
        val responseHeaders = iHeadersBuilder {
            header("cache-control", "max-age=86400")
        }
        val requestHeaders = iHeadersBuilder {
            header("cache-control", "no-cache")
        }
        val cachedResponseTime = Clock.System.now().toEpochMilliseconds() - 60000
        val status = CacheControlFreshnessCheckerImpl().invoke(
            requestHeaders = requestHeaders,
            responseHeaders = responseHeaders,
            responseLastValidated = cachedResponseTime,
            responseFirstStoredTime = cachedResponseTime,
        )

        assertFalse(status.isFresh)
    }


}