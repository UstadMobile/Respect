package world.respect.app.domain.usecase.strand

import world.respect.app.viewmodel.CurriculumStrand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class GetStrandsByCurriculumIdUseCase {
    suspend operator fun invoke(curriculumId: String): Flow<List<CurriculumStrand>> {
        return StrandStorage.strands.map { allStrands ->
            allStrands.filter { it.curriculumId == curriculumId }
                .map { strand ->
                    CurriculumStrand(
                        id = strand.id,
                        name = strand.name,
                        description = strand.description,
                        isActive = strand.isActive
                    )
                }
        }
    }
}