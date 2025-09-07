package world.respect.datalayer.ext

import com.ustadmobile.ihttp.headers.asIHttpHeaders
import io.github.aakira.napier.Napier
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.toHttpDate
import io.ktor.util.date.GMTDate
import world.respect.datalayer.networkvalidation.BaseDataSourceValidationHelper

/**
 * Add if-modified-since and if-none-match headers to the request using the validationHelper.
 *
 * This MUST be called AFTER the url is set and any potential VARY headers are set.
 */
suspend fun HttpRequestBuilder.addCacheValidationHeaders(
    validationHelper: BaseDataSourceValidationHelper
) {
    val urlBuilt = this.url.build()
    val validationInfo = validationHelper.getValidationInfo(
        url = urlBuilt,
        requestHeaders = this.headers.build().asIHttpHeaders(),
    )

    validationInfo?.lastModified?.takeIf { it > 0 }?.also { lastMod ->
        val ifModSinceDate = GMTDate(lastMod).toHttpDate()
        Napier.d("Cache Validation header: If-Modified-Since: $urlBuilt Set If-Modified-Since=$ifModSinceDate")
        headers[HttpHeaders.IfModifiedSince] = ifModSinceDate
    }

    validationInfo?.etag?.also { etag ->
        headers[HttpHeaders.IfNoneMatch] = etag
    }

}