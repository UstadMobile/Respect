package world.respect.lib.opds.model.ext

import world.respect.lib.opds.model.ReadiumLink

fun ReadiumLink.hasRel(relationship: String): Boolean {
    return this.rel?.let { rels -> relationship in rels } ?: false
}
