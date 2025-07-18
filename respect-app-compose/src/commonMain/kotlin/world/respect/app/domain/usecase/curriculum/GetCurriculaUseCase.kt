package world.respect.app.domain.usecase.curriculum

import world.respect.app.domain.models.Curriculum
import kotlinx.coroutines.flow.Flow

class GetCurriculaUseCase {
    suspend operator fun invoke(): Flow<List<Curriculum>> {
        return CurriculumStorage.curricula
    }
}