package world.respect.lib.opds.model

val LEARNING_UNIT_MIME_TYPES = listOf("text/html", "application/xml", "application/html+xml")

/**
 * Relationship for app opds link indicating a default lesson catalog
 */
const val REL_RESPECT_DEFAULT_CATALOG = "https://respect.ustadmobile.com/ns/default-lesson-catalog"

fun OpdsPublication.findLearningUnitAcquisitionLinks(): List<ReadiumLink> {
    return links.filter { link ->
        link.rel?.any { it.startsWith("http://opds-spec.org/acquisition") } == true &&
                LEARNING_UNIT_MIME_TYPES.any { link.type?.startsWith(it) == true }
    }
}

fun OpdsPublication.findSelfLinks(): List<ReadiumLink> {
    return links.filter {
        it.rel?.contains("self") == true
    }
}

fun OpdsPublication.findIcons(): List<ReadiumLink> {
    return images ?: emptyList()
}

fun OpdsPublication.respectAppDefaultLessonList(): ReadiumLink? {
    return links.firstOrNull {
        REL_RESPECT_DEFAULT_CATALOG in (it.rel ?: emptyList())
    }
}
