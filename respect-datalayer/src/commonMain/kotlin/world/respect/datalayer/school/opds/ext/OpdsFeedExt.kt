package world.respect.datalayer.school.opds.ext

import io.ktor.http.Url
import world.respect.lib.opds.model.OpdsFeed

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

