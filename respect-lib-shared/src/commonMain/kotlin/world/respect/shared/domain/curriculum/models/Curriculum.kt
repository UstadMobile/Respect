package world.respect.shared.domain.curriculum.models

data class Curriculum(
    val id: String,
    val name: String,
    val description: String,
    val isActive: Boolean

)
data class CurriculumStrand(
    val id: String,
    val name: String,
    val description: String = "",
    val isActive: Boolean = true,
)
data class SaveStrandParams(
    val curriculumId: String,
    val strandId: String?,
    val name: String,
    val learningObjectives: String,
    val outcomes: String
)
data class StrandEntity(
    val id: String,
    val curriculumId: String,
    val name: String,
    val description: String,
    val isActive: Boolean
)