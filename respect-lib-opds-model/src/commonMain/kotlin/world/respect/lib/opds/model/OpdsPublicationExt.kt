package world.respect.lib.opds.model

import world.respect.lib.opds.model.ext.hasRel

val LEARNING_UNIT_MIME_TYPES = listOf("text/html", "application/xml", "application/html+xml")

/**
 * Relationship for app opds link indicating a default lesson catalog
 */
const val REL_RESPECT_DEFAULT_CATALOG = "https://respect.ustadmobile.com/ns/default-lesson-catalog"

/**
 * Relationship for a link to a launchable app manifest
 */
const val REL_LAUNCHABLE_APP = "https://id.openeel.org/rel/launchable-app"

/**
 * Relationship for a link to a tincan.xml file
 */
const val REL_TINCAN_XML = "https://id.openeel.org/rel/tincanxml"

fun OpdsPublication.findLearningUnitAcquisitionLinks(): List<ReadiumLink> {
    return links.filter { link ->
        link.rel?.any {
            it.startsWith("http://opds-spec.org/acquisition") } == true &&
                LEARNING_UNIT_MIME_TYPES.any { link.type?.startsWith(it) == true
        }
    }
}

fun OpdsPublication.findSelfLinks(): List<ReadiumLink> {
    return links.filter {
        it.rel?.contains("self") == true
    }
}

fun OpdsPublication.findHighlightCardLinks(): List<ReadiumLink> {
    return links.filter {
        it.rel?.contains("https://id.openeel.org/rel/app-highlight-card") == true
    }
}

fun OpdsPublication.findLicenseLink(): ReadiumLink? =
    links.firstOrNull { it.rel?.contains("license") == true }

fun OpdsPublication.findTermsOfServiceLink(): ReadiumLink? =
    links.firstOrNull { it.rel?.contains("terms-of-service") == true }

fun OpdsPublication.findAppStoreAndroidLinks() : List<ReadiumLink> {
    return links.filter {
        it.hasRel("https://id.openeel.org/rel/appstore-android")
    }
}

fun OpdsPublication.findAppStoreAndroidLink(): ReadiumLink? =
    links.firstOrNull { it.rel?.contains("https://id.openeel.org/rel/appstore-android") == true }

fun OpdsPublication.findCollection(): ReadiumLink? =
    links.firstOrNull {
        it.rel?.contains("collection") == true
    } ?: respectAppManifestDefaultLessonList()


fun OpdsPublication.findLaunchableAppLink(): ReadiumLink? =
    links.firstOrNull { it.rel?.contains(REL_LAUNCHABLE_APP) == true }

fun OpdsPublication.findTinCanXmlLink(): ReadiumLink? =
    links.firstOrNull { it.rel?.contains(REL_TINCAN_XML) == true }

fun OpdsPublication.findIcons(): List<ReadiumLink> {
    return images ?: emptyList()
}

fun OpdsPublication.respectAppManifestDefaultLessonList(): ReadiumLink? {
    return links.firstOrNull {
        REL_RESPECT_DEFAULT_CATALOG in (it.rel ?: emptyList())
    }
}
