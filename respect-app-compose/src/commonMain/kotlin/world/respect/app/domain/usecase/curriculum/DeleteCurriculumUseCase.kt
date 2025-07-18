package world.respect.app.domain.usecase.curriculum

import world.respect.app.domain.usecase.strand.StrandStorage

class DeleteCurriculumUseCase {
    suspend operator fun invoke(curriculumId: String): Result<Unit> {
        return try {
            require(curriculumId.isNotBlank()) { "Curriculum ID cannot be blank" }

            kotlinx.coroutines.delay(DELETE_OPERATION_DELAY_MS)

            val currentList = CurriculumStorage.curricula.value.toMutableList()
            val removed = currentList.removeAll { it.id == curriculumId }

            if (removed) {
                CurriculumStorage.curricula.value = currentList
                StrandStorage.removeStrandsByCurriculumId(curriculumId)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val DELETE_OPERATION_DELAY_MS = 300L
    }
}