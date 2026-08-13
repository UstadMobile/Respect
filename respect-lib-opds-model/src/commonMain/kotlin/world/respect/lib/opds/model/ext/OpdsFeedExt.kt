package world.respect.lib.opds.model.ext

import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink

fun OpdsFeed.feedIconLinkOrNull(): ReadiumLink? {
    return links.filterByHasRel("icon").firstOrNull()
}

fun OpdsFeed.allPublications(): List<OpdsPublication> {
    return publications.orEmpty() + groups.orEmpty().flatMap {
        it.publications.orEmpty()
    }
}
