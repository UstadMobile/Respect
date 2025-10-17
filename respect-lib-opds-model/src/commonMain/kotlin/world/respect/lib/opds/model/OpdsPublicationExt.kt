package world.respect.lib.opds.model

val LEARNING_UNIT_MIME_TYPES = listOf("text/html", "application/xml", "application/html+xml")

fun OpdsPublication.findLearningUnitAcquisitionLinks(): List<ReadiumLink> {
    return links.filter { link ->
        link.rel?.any { it.startsWith("http://opds-spec.org/acquisition") } == true &&
                LEARNING_UNIT_MIME_TYPES.any { link.type?.startsWith(it) == true }
    }
}