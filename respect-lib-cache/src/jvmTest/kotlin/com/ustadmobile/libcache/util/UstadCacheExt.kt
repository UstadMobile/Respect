package com.ustadmobile.libcache.util

import com.ustadmobile.ihttp.headers.IHttpHeader
import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.requestBuilder
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.StoreResult
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheImpl
import com.ustadmobile.libcache.novarysearch.NoVarySearch
import com.ustadmobile.libcache.novarysearch.normalizeForNoVarySearchIfNotNull
import com.ustadmobile.libcache.response.HttpPathResponse
import io.ktor.http.Headers
import io.ktor.http.Url
import io.ktor.http.toHttpDate
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.flow.first
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File

data class FileStoredAsUrl(
    val request: IHttpRequest,
    val response: IHttpResponse,
    val storeResult: StoreResult,
    val file: File,
)

suspend fun UstadCache.storeFileAsUrl(
    testFile: File,
    testUrl: String,
    mimeType: String,
    requestHeaders: List<IHttpHeader> = emptyList(),
    extraHeaders: Headers? = null,
): FileStoredAsUrl {
    val urlObj = Url(testUrl)

    extraHeaders?.also { extraHeadersVal ->
        setExtraResponseHeaders(
            url = urlObj.normalizeForNoVarySearchIfNotNull(extraHeadersVal["No-Vary-Search"]),
            extraResponseHeaders = extraHeaders,
        )
    }

    val request = requestBuilder {
        url = testUrl
        requestHeaders.forEach {
            header(it.name, it.value)
        }
    }

    val response = HttpPathResponse(
        path = Path(testFile.absolutePath),
        fileSystem = SystemFileSystem,
        mimeType = mimeType,
        request = request,
        extraHeaders = iHeadersBuilder {
            header(
                name = "Last-Modified",
                value = GMTDate( testFile.lastModified()).toHttpDate()
            )
        }
    )

    val storeResult = store(
        listOf(
            CacheEntryToStore(
                request = request,
                response = response,
            )
        ),
    )

    return FileStoredAsUrl(request, response, storeResult.first(), testFile)
}

suspend fun UstadCacheImpl.awaitUpdatesCommitted() {
    updateBacklogSize.first { it == 0 }
}