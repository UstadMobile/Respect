package world.respect.datalayer.school.opds.ext

import io.ktor.http.Url
import world.respect.datalayer.DataLoadMetaInfo
import world.respect.lib.opds.model.OpdsFeed
import world.respect.libutil.util.time.systemTimeInMillis

fun OpdsFeed.selfUrl(): Url? {
    return links.firstOrNull { "self" in (it.rel ?: emptyList()) }?.let {
        Url(it.href)
    }
}

fun OpdsFeed.requireSelfUrl(): Url {
    return selfUrl() ?: throw IllegalStateException("No self url found")
}

/**
 *
 */
fun OpdsFeed.withAbsoluteSelfUrl(urlLoaded: Url): OpdsFeed {
    return copy(
        links = links.map { link ->
            if("self" in (link.rel ?: emptyList())) {
                link.copy(href = urlLoaded.toString())
            }else {
                link
            }
        }
    )
}


/**
 * Playlists that are being stored:
 *
 * 1) Url is as per the self link (which MUST be an absolute URL based on the school url).
 * 2) Etag is the metainfo.modified (which MUST NOT be null)
 * 3) last modified header is the stored time : see respect-datalayer-repository for notes on why
 *    this http header uses the stored time NOT the time of the actual modification.
 */
fun OpdsFeed.dataLoadMetaInfoForPlaylist() : DataLoadMetaInfo {
    val modified = metadata.modified ?: throw IllegalArgumentException()

    return DataLoadMetaInfo(
        url = requireSelfUrl(),
        lastModified = systemTimeInMillis(),
        etag = modified.toString(),
    )
}
