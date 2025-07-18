package world.respect.app.domain.usecase.curriculum

import world.respect.app.domain.models.Curriculum
import kotlinx.coroutines.flow.MutableStateFlow

object CurriculumStorage {
    val curricula = MutableStateFlow<List<Curriculum>>(emptyList())
}