package world.respect.app.domain.usecase.curriculum

import world.respect.app.domain.models.Curriculum

class GetCurriculumByIdUseCase {
    suspend operator fun invoke(curriculumId: String): Curriculum? {
        return CurriculumStorage.curricula.value.find { it.id == curriculumId }
    }
}