package world.respect.lib.opds.model.ext

import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.ReadiumLink

fun OpdsFeed.feedIconLinkOrNull(): ReadiumLink? {
    return links.filterByHasRel("icon").firstOrNull()
}
