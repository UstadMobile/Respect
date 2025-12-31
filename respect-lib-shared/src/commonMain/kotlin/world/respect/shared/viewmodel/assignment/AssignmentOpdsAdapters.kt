package world.respect.shared.viewmodel.assignment

import io.ktor.http.Url
import world.respect.datalayer.school.model.AssignmentLearningUnitRef
import world.respect.lib.opds.model.OpdsGroup

/**
 * Convert OpdsGroup publications to AssignmentLearningUnitRef list.
 * Extracts learning unit manifest URLs and their corresponding app manifest URLs
 * from OPDS publications.
 */
fun OpdsGroup.toAssignmentLearningUnitRefs(): List<AssignmentLearningUnitRef> {
    val publications = this.publications ?: emptyList()

    return publications.mapNotNull { publication ->
        try {
            val acquisitionLink = publication.links.firstOrNull { link ->
                link.rel?.any { it.startsWith("http://opds-spec.org/acquisition") } == true
            } ?: return@mapNotNull null

            val publicationUrl = Url(acquisitionLink.href)

            val appManifestLink = publication.links.firstOrNull { link ->
                link.rel?.contains("http://opds-spec.org/compatible-app") == true
            }

            val appManifestUrl = appManifestLink?.let { Url(it.href) }
                ?: return@mapNotNull null

            AssignmentLearningUnitRef(
                learningUnitManifestUrl = publicationUrl,
                appManifestUrl = appManifestUrl
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}