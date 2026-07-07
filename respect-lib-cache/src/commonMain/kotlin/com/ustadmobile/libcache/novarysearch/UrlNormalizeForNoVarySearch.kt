package com.ustadmobile.libcache.novarysearch

import io.ktor.http.URLBuilder
import io.ktor.http.Url

fun Url.normalizeForNoVarySearch(noVarySearch: NoVarySearch): Url {
    return URLBuilder(this).normalizeForNoVarySearch(noVarySearch).build()
}

fun Url.normalizeForNoVarySearchIfNotNull(
    noVarySearchHeader: String?
): Url {
    return if(noVarySearchHeader != null) {
        normalizeForNoVarySearch(NoVarySearch.parse(noVarySearchHeader))
    }else {
        this
    }
}

fun Url.removeAllSearchParams() : Url {
    return URLBuilder(this).apply {
        encodedParameters.clear()
    }.build()
}
