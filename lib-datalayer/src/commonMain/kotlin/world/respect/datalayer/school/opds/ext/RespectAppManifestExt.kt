package world.respect.datalayer.school.opds.ext

import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.REL_RESPECT_DEFAULT_CATALOG
import world.respect.lib.opds.model.ReadiumLink
import world.respect.lib.opds.model.ReadiumMetadata

fun RespectAppManifest.asOpdsPublication(): OpdsPublication {
    return OpdsPublication(
        metadata = ReadiumMetadata(
            title = name,
        ),
        links = listOf(
            ReadiumLink(
                href = learningUnits.toString(),
                rel = listOf(REL_RESPECT_DEFAULT_CATALOG),
                type = "application/opds+json",
            )
        ),
        images = icon?.let {
            listOf(ReadiumLink(href = it.toString()))
        } ?: emptyList()
    )
}
