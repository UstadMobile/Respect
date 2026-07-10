package com.ustadmobile.libcache.novarysearch

import com.ustadmobile.libcache.util.LaunchNoVarySearchConstants
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

/**
 * Remove parameters with the given names
 */
fun Url.removeSearchParamsByNames(paramNames: List<String>): Url {
    return URLBuilder(this).apply {
        paramNames.forEach { name ->
            encodedParameters.remove(name)
        }
    }.build()
}

fun Url.removeLaunchSearchParams(): Url {
    return removeSearchParamsByNames(LaunchNoVarySearchConstants.LAUNCH_NO_VARY_PARAM_NAMES)
}