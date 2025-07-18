package world.respect.app.domain.usecase.strand
import kotlinx.coroutines.flow.MutableStateFlow

object StrandStorage {
    data class StrandEntity(
        val id: String,
        val curriculumId: String,
        val name: String,
        val description: String,
        val isActive: Boolean
    )

    val strands = MutableStateFlow<List<StrandEntity>>(emptyList())

    fun removeStrandsByCurriculumId(curriculumId: String) {
        val currentList = strands.value.toMutableList()
        currentList.removeAll { it.curriculumId == curriculumId }
        strands.value = currentList
    }
}