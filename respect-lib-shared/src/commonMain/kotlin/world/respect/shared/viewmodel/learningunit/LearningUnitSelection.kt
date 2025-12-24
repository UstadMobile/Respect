package world.respect.shared.viewmodel.learningunit

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import world.respect.lib.opds.model.OpdsPublication

/**
 * Represents a Learning Unit when selected by a user to be returned to another screen (e.g. as
 * part of selecting an assignment)
 */
@Serializable
data class LearningUnitSelection(
    val learningUnitManifestUrl: Url,
    val selectedPublication: OpdsPublication,
    val appManifestUrl: Url,
)

