package com.ustadmobile.ihttp.headers

import io.ktor.http.Headers

fun Headers.asRawString() : String {
    return this.names().flatMap {  name ->
        this.getAll(name)?.map { name to it } ?: emptyList()
    }.joinToString(separator = "\r\n") { "${it.first}: ${it.second}" }
}
