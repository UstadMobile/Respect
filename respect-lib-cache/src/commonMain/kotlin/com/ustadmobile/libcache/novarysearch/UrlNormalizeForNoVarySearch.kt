package com.ustadmobile.libcache.novarysearch

import io.ktor.http.URLBuilder
import io.ktor.http.Url

fun Url.normalizeForNoVarySearch(noVarySearch: NoVarySearch): Url {
    return URLBuilder(this).normalizeForNoVarySearch(noVarySearch).build()
}

fun Url.removeAllParams() : Url {
    return URLBuilder(this).apply {
        encodedParameters.clear()
    }.build()
}
