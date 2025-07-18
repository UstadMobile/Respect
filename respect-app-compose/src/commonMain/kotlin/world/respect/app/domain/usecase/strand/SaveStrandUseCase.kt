
package world.respect.app.domain.usecase.strand

import world.respect.app.viewmodel.CurriculumStrand

class SaveStrandUseCase {
    data class SaveStrandParams(
        val curriculumId: String,
        val strandId: String?,
        val name: String,
        val learningObjectives: String,
        val outcomes: String
    )

    suspend operator fun invoke(params: SaveStrandParams): Result<CurriculumStrand> {
        return try {
            require(params.curriculumId.isNotBlank()) { "Curriculum ID cannot be blank" }
            require(params.name.isNotBlank()) { "Strand name cannot be blank" }
            require(params.learningObjectives.isNotBlank()) { "Learning objectives cannot be blank" }
            require(params.outcomes.isNotBlank()) { "Outcomes cannot be blank" }

            kotlinx.coroutines.delay(SAVE_OPERATION_DELAY_MS)

            val strandId = params.strandId ?: generateStrandId()
            val description = buildStrandDescription(params.learningObjectives, params.outcomes)

            val strand = StrandStorage.StrandEntity(
                id = strandId,
                curriculumId = params.curriculumId,
                name = params.name,
                description = description,
                isActive = true
            )

            val currentList = StrandStorage.strands.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == strandId }

            if (existingIndex >= 0) {
                currentList[existingIndex] = strand
            } else {
                currentList.add(strand)
            }

            StrandStorage.strands.value = currentList

            val savedStrand = CurriculumStrand(
                id = strand.id,
                name = strand.name,
                description = strand.description,
                isActive = strand.isActive
            )

            Result.success(savedStrand)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateStrandId(): String {
        return "strand_${System.currentTimeMillis()}"
    }

    private fun buildStrandDescription(learningObjectives: String, outcomes: String): String {
        return "Learning Objectives: $learningObjectives\n\nExpected Outcomes: $outcomes"
    }

    companion object {
        private const val SAVE_OPERATION_DELAY_MS = 500L
    }
}