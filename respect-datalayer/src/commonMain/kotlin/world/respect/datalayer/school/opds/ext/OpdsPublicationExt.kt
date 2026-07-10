package world.respect.datalayer.school.opds.ext

import io.ktor.http.Url
import world.respect.lib.opds.model.OpdsPublication

fun OpdsPublication.withAbsoluteSelfUrl(urlLoaded: Url): OpdsPublication {
    return copy(links = links.withAbsoluteSelfLink(urlLoaded))
}

fun OpdsPublication.requireAbsoluteSelfUrl(): Url {
    return Url(
        links.first { it.hasRel("self") }.href
    )
}
