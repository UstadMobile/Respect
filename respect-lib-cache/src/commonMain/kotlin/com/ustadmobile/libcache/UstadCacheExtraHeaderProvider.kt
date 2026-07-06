package com.ustadmobile.libcache

import com.ustadmobile.ihttp.request.IHttpRequest
import io.ktor.http.Headers

fun interface UstadCacheExtraHeaderProvider {

    operator fun invoke(request: IHttpRequest): Headers?

}