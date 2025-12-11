package world.respect.shared.viewmodel.curriculum.mapping.model

import kotlinx.serialization.Serializable

@Serializable
data class CurriculumMapping(
    val uid: Long = System.currentTimeMillis(),
    val title: String = "",
    val description: String = "",
    val subject: String? = null,
    val grade: String? = null,
    val language: String? = null,
    val sections: List<CurriculumMappingSection> = emptyList()
)
