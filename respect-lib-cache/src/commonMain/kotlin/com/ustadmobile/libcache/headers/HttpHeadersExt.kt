package com.ustadmobile.libcache.headers

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.libcache.headers.CouponHeader.Companion.HEADER_ETAG_IS_INTEGRITY
import com.ustadmobile.libcache.headers.CouponHeader.Companion.HEADER_X_INTEGRITY

/**
 * By default we will use the integrity string as the etag so that validation works as expected
 * in a distributed fashion.
 */
fun IHttpHeaders.integrity(): String? {
    return if(get(HEADER_ETAG_IS_INTEGRITY) == "true") {
        get("etag")
    }else {
        get(HEADER_X_INTEGRITY)
    }
}

fun IHttpHeaders.requireIntegrity(): String {
    return integrity() ?: throw IllegalStateException("Headers do not include integrity")
}


/**
 * Shorthand to check if a header exists. Note that just checking names is not correct because header
 * names are case insensitive
 */
fun IHttpHeaders.containsHeader(headerName: String) = get(headerName) != null

/**
 * Shorthand to check if the given request headers contain cache validation info:
 * if-modified-since or if-none-match.
 */
fun IHttpHeaders.hasCacheValidators() : Boolean {
    return this.containsHeader("if-modified-since") ||
            this.containsHeader("if-none-match")
}
