package world.respect.app.domain

import world.respect.app.domain.models.Curriculum
import world.respect.shared.generated.resources.Res
import kotlinx.coroutines.delay

class SaveCurriculumUseCase {

    companion object {
        const val MOCK_DELAY_MS = 500L
    }

    data class SaveCurriculumResult(
        val isSuccess: Boolean,
        val curriculum: Curriculum?,
        val errorMessage: String?
    ) {
        companion object {
            fun success(curriculum: Curriculum) = SaveCurriculumResult(
                isSuccess = true,
                curriculum = curriculum,
                errorMessage = null
            )

            fun failure(errorMessage: String) = SaveCurriculumResult(
                isSuccess = false,
                curriculum = null,
                errorMessage = errorMessage
            )
        }
    }

    /**
     * Invoke the use case to save curriculum.
     *
     * @param curriculum the curriculum data to save.
     * @param errorInvalidData localized error string for invalid data.
     * @param errorUnknown localized error string for unknown errors.
     */
    suspend operator fun invoke(
        curriculum: Curriculum?,
        errorInvalidData: String,
        errorUnknown: String
    ): SaveCurriculumResult {
        val data = curriculum ?: return SaveCurriculumResult.failure(errorInvalidData)

        val name = data.name
        val id = data.id
        val description = data.description

        if (name.isBlank() || id.isBlank() || description.isBlank()) {
            return SaveCurriculumResult.failure(errorInvalidData)
        }

        return try {
            delay(MOCK_DELAY_MS)
            SaveCurriculumResult.success(data)
        } catch (exception: Exception) {
            val message = exception.message ?: errorUnknown
            SaveCurriculumResult.failure(message)
        }
    }
}
