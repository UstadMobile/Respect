package world.respect.shared.viewmodel.curriculum.mapping.model

import kotlinx.serialization.Serializable

/**
 * @property href Absolute URL to the OPDS publication linked (NOT the Learning Unit ID URL).
 */
@Serializable
data class CurriculumMappingSectionLink(
    val uid: Long = System.currentTimeMillis(),
    val href: String,
    val title: String? = "",
    val description: String? = null
)
