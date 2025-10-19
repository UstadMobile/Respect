package world.respect.datalayer.school.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
data class AssignmentLearningUnitRef(
    val learningUnitManifestUrl: Url,
    val appManifestUrl: Url,
)
