package world.respect.shared.viewmodel.curriculum.mapping.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
class CurriculumMappingSectionLink(
    val href: Url,
    val title: String? = ""
)
