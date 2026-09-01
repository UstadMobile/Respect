package world.respect.shared.util.ext

import io.ktor.http.Url
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.libutil.ext.resolve

fun OpdsPublication.resolve(baseUrl: Url) : OpdsPublication {
    return copy(
        links = links.resolveAll(baseUrl),
        images = images?.resolveAll(baseUrl),
        readingOrder = readingOrder?.resolveAll(baseUrl),
        resources = resources?.resolveAll(baseUrl),
        toc = toc?.resolveAll(baseUrl),
    )
}

/**
 * Create an Activity ID for the legacy launch schema where the launcher app would simply look for
 * an acquisition link.
 */
fun OpdsPublication.legacyActivityIdForLink(
    link: ReadiumLink,
    publicationUrl: Url,
): String {
    return metadata.identifier?.toString() ?: publicationUrl.resolve(link.href).toString()
}
