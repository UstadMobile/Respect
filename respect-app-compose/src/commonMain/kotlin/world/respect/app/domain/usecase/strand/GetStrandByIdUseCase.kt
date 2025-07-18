package world.respect.app.domain.usecase.strand

import world.respect.app.viewmodel.CurriculumStrand

class GetStrandByIdUseCase {
    suspend operator fun invoke(strandId: String): CurriculumStrand? {
        val strand = StrandStorage.strands.value.find { it.id == strandId }
        return strand?.let {
            CurriculumStrand(
                id = it.id,
                name = it.name,
                description = it.description,
                isActive = it.isActive
            )
        }
    }
}