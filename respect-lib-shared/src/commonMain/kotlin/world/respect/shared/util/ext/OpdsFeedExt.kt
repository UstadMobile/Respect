package world.respect.shared.util.ext

import io.ktor.http.Url
import world.respect.lib.opds.model.OpdsFeed
import world.respect.libutil.ext.resolve

/**
 * Resolves all relative URLs (navigation, images, groups, alternates)
 * inside an OpdsFeed against the given base URL.
 */

fun OpdsFeed.resolve(baseUrl: Url): OpdsFeed {
    return this.copy(
        navigation = navigation?.map { nav ->
            nav.copy(
                href = baseUrl.resolve(nav.href).toString(),
                alternate = nav.alternate?.map { alt ->
                    alt.copy(
                        href = baseUrl.resolve(alt.href).toString()
                    )
                }
            )
        } ?: emptyList(),

        publications = publications?.map { publication ->
            publication.copy(
                images = publication.images?.map { image ->
                    image.copy(
                        href = baseUrl.resolve(image.href).toString()
                    )
                }
            )
        } ?: emptyList(),

        groups = groups?.map { group ->
            group.copy(
                publications = group.publications?.map { pub ->
                    pub.copy(
                        images = pub.images?.map { img ->
                            img.copy(
                                href = baseUrl.resolve(img.href).toString()
                            )
                        }
                    )
                },
                navigation = group.navigation?.map { nav ->
                    nav.copy(
                        href = baseUrl.resolve(nav.href).toString(),
                        alternate = nav.alternate?.map { alt ->
                            alt.copy(
                                href = baseUrl.resolve(alt.href).toString()
                            )
                        }
                    )
                }
            )
        } ?: emptyList()
    )
}
