package world.respect.shared.viewmodel.curriculum.mapping.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
data class CurriculumMappingSectionLink(
    val uid: Long = System.currentTimeMillis(),
    val href: String,
    val title: String? = ""
)
