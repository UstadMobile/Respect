package world.respect.shared.viewmodel.curriculum.mapping.model

import kotlinx.serialization.Serializable

@Serializable
data class CurriculumMapping(
    val title: String = "",
    val sections: List<CurriculumMappingSection> = emptyList()
)
