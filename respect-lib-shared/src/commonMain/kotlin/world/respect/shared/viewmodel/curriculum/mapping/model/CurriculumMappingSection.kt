package world.respect.shared.viewmodel.curriculum.mapping.model

import kotlinx.serialization.Serializable

@Serializable
data class CurriculumMappingSection(
    val title: String,
    val items: List<CurriculumMappingSectionLink> = emptyList()
)
