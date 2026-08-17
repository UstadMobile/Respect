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

fun OpdsFeed.getPublicationsByIndexes(
    indexes: Collection<OpdsFeedItemIndex>
) : List<OpdsPublication>{
    return publications?.filterIndexed { index, _ ->
        OpdsFeedItemIndex(-1, index) in indexes
    }.orEmpty() + groups?.flatMapIndexed { groupIndex: Int, group ->
        group.publications?.filterIndexed { index, _ ->
            OpdsFeedItemIndex(groupIndex, index) in indexes
        }.orEmpty()
    }.orEmpty()
}

fun OpdsFeed.getNavigationLinkByIndex(opdsFeedItemIndex: OpdsFeedItemIndex): ReadiumLink {
    return if(opdsFeedItemIndex.groupIndex >= 0){
        this.groups?.get(opdsFeedItemIndex.groupIndex)?.navigation?.get(opdsFeedItemIndex.index)
            ?: throw IllegalArgumentException("No navigation link at $opdsFeedItemIndex")
    }else {
        this.navigation?.get(opdsFeedItemIndex.index) ?: throw IllegalArgumentException("No navigation at $opdsFeedItemIndex")
    }
}

fun OpdsFeed.getNavigationLinksByIndexes(
    indexes: Collection<OpdsFeedItemIndex>
): List<ReadiumLink> {
    return navigation?.filterIndexed { index, _ ->
        OpdsFeedItemIndex(-1, index) in indexes
    }.orEmpty() + groups?.flatMapIndexed { groupIndex: Int, group ->
        group.navigation?.filterIndexed { index, _ ->
            OpdsFeedItemIndex(groupIndex, index) in indexes
        }.orEmpty()
    }.orEmpty()
}
