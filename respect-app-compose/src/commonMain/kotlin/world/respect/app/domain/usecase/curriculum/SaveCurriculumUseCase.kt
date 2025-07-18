package world.respect.app.domain.usecase.curriculum

import world.respect.app.domain.models.Curriculum

class SaveCurriculumUseCase {
    suspend operator fun invoke(curriculum: Curriculum): Result<Curriculum> {
        return try {
            require(curriculum.name.isNotBlank()) { "Curriculum name cannot be blank" }
            require(curriculum.id.isNotBlank()) { "Curriculum ID cannot be blank" }
            kotlinx.coroutines.delay(SAVE_OPERATION_DELAY_MS)

            val currentList = CurriculumStorage.curricula.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == curriculum.id }

            if (existingIndex >= 0) {
                currentList[existingIndex] = curriculum
            } else {
                currentList.add(curriculum)
            }

            CurriculumStorage.curricula.value = currentList
            Result.success(curriculum)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val SAVE_OPERATION_DELAY_MS = 500L
    }
}