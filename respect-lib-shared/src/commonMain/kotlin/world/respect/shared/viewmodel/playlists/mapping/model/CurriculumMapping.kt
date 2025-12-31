package world.respect.shared.viewmodel.playlists.mapping.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
data class CurriculumMapping(
    val uid: Long = System.currentTimeMillis(),
    val title: String = "",
    val description: String = "",
    val sections: List<CurriculumMappingSection> = emptyList(),
    val createdBy: String? = null,
    val isSchoolWide: Boolean = false,
    val schoolUrl: Url? = null,
)
