package com.ustadmobile.libcache.md5

import io.ktor.http.Url
import io.ktor.util.encodeBase64

fun Md5Digest.urlKey(string: String): String {
    return digest(string.encodeToByteArray()).encodeBase64()
}

fun Md5Digest.urlHash(url: Url): String {
    return digest(url.toString().encodeToByteArray()).encodeBase64()
}
