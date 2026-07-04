package com.ustadmobile.libcache.novarysearch

import okhttp3.HttpUrl

fun HttpUrl.Builder.applyNoVarySearch(
    noVarySearch: NoVarySearch,
    originalUrl: HttpUrl,
): HttpUrl.Builder {
    return this
}