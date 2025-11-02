package world.respect.shared.util.ext

import io.ktor.http.Url
import world.respect.lib.opds.model.OpdsPublication

fun OpdsPublication.resolve(baseUrl: Url) : OpdsPublication {
    return copy(
        links = links.resolveAll(baseUrl),
        images = images?.resolveAll(baseUrl),
        readingOrder = readingOrder?.resolveAll(baseUrl),
        resources = resources?.resolveAll(baseUrl),
        toc = toc?.resolveAll(baseUrl),
    )
}