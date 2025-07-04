package world.respect.libutil.ext

import io.ktor.http.Url
import io.ktor.http.toURI

/**
 * Resolve an href (that could be absolute or relative)
 *
 * @receiver a KTOR Url (an absolute URL)
 */
fun Url.resolve(href: String): Url {
    return Url(toURI().resolve(href).toString())
}
