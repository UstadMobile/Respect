package world.respect.shared.domain.strand

import world.respect.shared.domain.curriculum.models.CurriculumStrand
import world.respect.shared.domain.curriculum.models.SaveStrandParams
import world.respect.shared.domain.curriculum.models.StrandEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay

interface GetStrandsByCurriculumIdUseCase {
    suspend operator fun invoke(curriculumId: String): Flow<List<CurriculumStrand>>
}

interface GetStrandByIdUseCase {
    suspend operator fun invoke(strandId: String): CurriculumStrand?
}

interface SaveStrandUseCase {
    suspend operator fun invoke(params: SaveStrandParams): Result<CurriculumStrand>
}

object StrandMockData {
    val strands = MutableStateFlow<List<StrandEntity>>(
        listOf(
            StrandEntity(
                DEFAULT_STRAND_ID,
                DEFAULT_CURRICULUM_ID,
                DEFAULT_STRAND_NAME,
                DEFAULT_STRAND_DESCRIPTION,
                true
            ),
        )
    )

    fun removeStrandsByCurriculumId(curriculumId: String) {
        val currentList = strands.value.toMutableList()
        currentList.removeAll { it.curriculumId == curriculumId }
        strands.value = currentList
    }

    private const val DEFAULT_STRAND_ID = "s1"
    private const val DEFAULT_CURRICULUM_ID = "1"
    private const val DEFAULT_STRAND_NAME = "Algebra"
    private const val DEFAULT_STRAND_DESCRIPTION = "Learning Objectives: Basic algebra\n\nExpected Outcomes: Solve equations"
}

class GetStrandsByCurriculumIdUseCaseMock : GetStrandsByCurriculumIdUseCase {
    override suspend operator fun invoke(curriculumId: String): Flow<List<CurriculumStrand>> {
        return StrandMockData.strands.map { allStrands ->
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

class GetStrandByIdUseCaseMock : GetStrandByIdUseCase {
    override suspend operator fun invoke(strandId: String): CurriculumStrand? {
        val strand = StrandMockData.strands.value.find { it.id == strandId }
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

class SaveStrandUseCaseMock : SaveStrandUseCase {
    override suspend operator fun invoke(params: SaveStrandParams): Result<CurriculumStrand> {
        return try {
            require(params.curriculumId.isNotBlank()) { "field_required" }
            require(params.name.isNotBlank()) { "field_required" }
            require(params.learningObjectives.isNotBlank()) { "field_required" }
            require(params.outcomes.isNotBlank()) { "field_required"}

            delay(MOCK_SAVE_DELAY_MS)

            val strandId = params.strandId ?: generateStrandId()
            val description = buildStrandDescription(params.learningObjectives, params.outcomes)

            val strand = StrandEntity(
                id = strandId,
                curriculumId = params.curriculumId,
                name = params.name,
                description = description,
                isActive = true
            )

            val currentList = StrandMockData.strands.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == strandId }

            if (existingIndex >= 0) {
                currentList[existingIndex] = strand
            } else {
                currentList.add(strand)
            }

            StrandMockData.strands.value = currentList

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
        return "${STRAND_ID_PREFIX}${System.currentTimeMillis()}"
    }


    private fun buildStrandDescription(learningObjectives: String, outcomes: String): String {
        return "Learning Objectives: $learningObjectives\n\nExpected Outcomes: $outcomes"
    }
    companion object {
        private const val MOCK_SAVE_DELAY_MS = 500L
        private const val STRAND_ID_PREFIX = "strand_"

    }
}