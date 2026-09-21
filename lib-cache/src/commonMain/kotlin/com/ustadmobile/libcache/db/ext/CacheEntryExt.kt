package com.ustadmobile.libcache.db.ext

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.libcache.UstadCache.Companion.HEADER_LAST_VALIDATED_TIMESTAMP
import com.ustadmobile.libcache.db.entities.CacheEntry

fun CacheEntry.makeHttpResponseHeaders(
    extraHeaders: String?
): IHttpHeaders = iHeadersBuilder {
    takeFrom(IHttpHeaders.fromString(responseHeaders))
    extraHeaders?.also {
        takeFrom(IHttpHeaders.fromString(it))
    }
    header(HEADER_LAST_VALIDATED_TIMESTAMP, lastValidated.toString())
}