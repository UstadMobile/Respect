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

fun OpdsFeed.getPublicationByIndex(opdsFeedItemIndex: OpdsFeedItemIndex): OpdsPublication {
    return if(opdsFeedItemIndex.groupIndex >= 0) {
        this.groups?.get(opdsFeedItemIndex.index)?.publications?.get(opdsFeedItemIndex.index)
            ?: throw IllegalArgumentException("No publication at index:${opdsFeedItemIndex.groupIndex}/index:${opdsFeedItemIndex.index}")
    }else {
        this.publications?.get(opdsFeedItemIndex.index) ?: throw IllegalArgumentException("No publications")
    }
}
