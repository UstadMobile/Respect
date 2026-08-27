package world.respect.shared.util.ext

import io.ktor.http.Url
import world.respect.lib.opds.model.Publication

fun Publication.resolve(baseUrl: Url) : Publication {
    return copy(
        links = links.resolveAll(baseUrl),
        images = images?.resolveAll(baseUrl),
        readingOrder = readingOrder?.resolveAll(baseUrl),
        resources = resources?.resolveAll(baseUrl),
        toc = toc?.resolveAll(baseUrl),
    )
}

fun Publication.key(
    indexInFeed: Int,
) : String {
    return metadata.identifier?.toString() ?: indexInFeed.toString()
}
