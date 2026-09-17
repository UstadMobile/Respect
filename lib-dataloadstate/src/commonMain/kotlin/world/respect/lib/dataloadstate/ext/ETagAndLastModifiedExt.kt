package world.respect.lib.dataloadstate.ext

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.http.toHttpDate
import world.respect.lib.dataloadstate.ETagAndLastModified
import world.respect.lib.dataloadstate.datetime.toGMTDate

fun ETagAndLastModified.toHeaders(): Headers {
    return headers {
        etag?.also {
            set(HttpHeaders.ETag, it)
        }

        lastModified?.also {
            set(HttpHeaders.LastModified, it.toGMTDate().toHttpDate())
        }
    }
}
