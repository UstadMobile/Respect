package com.ustadmobile.libcache.util

import io.ktor.http.URLProtocol

/**
 * Return true if the protocol is http or https
 */
fun URLProtocol.isHttp() = (this == URLProtocol.HTTPS) || (this == URLProtocol.HTTP)