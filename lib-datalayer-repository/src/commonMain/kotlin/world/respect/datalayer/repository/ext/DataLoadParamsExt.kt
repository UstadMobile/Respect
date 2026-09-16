package world.respect.datalayer.repository.ext

import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadParams

/**
 * Copy the receiver [DataLoadParams] to be used to validate the data from a remote data source.
 * It will add (where available) the IfModifiedSince and IfNonMatch headers to the [DataLoadParams]
 * that are returned
 *
 * @receiver the original [DataLoadParams] e.g. as supplied to a data source function.
 * @param localMetaInfo the [DataLoadMetaInfo] from loading the data locally that needs to be validated
 *
 * @return [DataLoadParams] that include IfModifiedSince and IfNoneMatch headers if the
 *         last-modified and etag headers respectively are present in the localMetaInfo.
 */
fun DataLoadParams.copyToValidateOnRemote(
    localMetaInfo: DataLoadMetaInfo
) : DataLoadParams {
    return copy(
        requestHeaders = headers {
            appendAll(this@copyToValidateOnRemote.requestHeaders)

            /**
             * Using the last-modified header here will prevent data just updated locally
             * from being overwritten. The last-modified header on the server will be the
             * time that data was actually stored on the server, NOT when it was actually
             * modified on the client. It is however close enough.
             */
            localMetaInfo.headers[HttpHeaders.LastModified]?.also {
                set(HttpHeaders.IfModifiedSince, it)
            }

            localMetaInfo.headers[HttpHeaders.ETag]?.also {
                set(HttpHeaders.IfNoneMatch, it)
            }
        }
    )
}
