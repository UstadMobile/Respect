package world.respect.datasource

import io.ktor.http.HttpMessage
import io.ktor.http.Url
import io.ktor.http.etag
import world.respect.datasource.ext.lastModifiedAsLong

/**
 * Combined metadata (e.g. data about data) on loaded data. This includes the loading status and
 * validation info (e.g. last modified time and etags as available).
 *
 * @param lastModified when the data was last modified. When data is fetched over http, this can be
 *         - determined by subtracting the age header (delta seconds) from the system clock.
 *         - parsed from the last-modified header.
 *
 *         When loading and storing local data, the last modified time is tracked as a field in the
 *         database.
 *
 *         When this is unknown/not applicable - e.g. whilst still awaiting a response, then it will
 *         be -1L
 *
 * @param etag etag as provided by the origin server (if any)
 *
 * @param url the URL
 */
data class DataLoadMetaInfo(
    val status: LoadingStatus,

    val lastModified: Long = -1,

    val etag: String? = null,

    val url: Url? = null,
) {

    fun requireUrl() = url ?: throw IllegalStateException("requireUrl: load meta info has no url")

    companion object {

        fun fromHttpMessage(
            url: Url,
            message: HttpMessage,
        ) = DataLoadMetaInfo(
            status = LoadingStatus.LOADED,
            lastModified = message.lastModifiedAsLong(),
            etag = message.etag(),
            url = url,
        )

    }

}


