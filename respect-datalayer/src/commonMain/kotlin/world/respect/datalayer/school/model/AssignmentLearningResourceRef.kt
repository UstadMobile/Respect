package world.respect.datalayer.school.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
data class AssignmentLearningResourceRef(
    val learningUnitManifestUrl: Url,
    val appManifestUrl: Url,
)
