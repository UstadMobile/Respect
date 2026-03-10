package world.respect.datalayer.school.opds.ext

import world.respect.lib.opds.model.ReadiumLink

fun ReadiumLink.hasRel(relationship: String): Boolean {
    return relationship in (this.rel ?: emptyList())
}
