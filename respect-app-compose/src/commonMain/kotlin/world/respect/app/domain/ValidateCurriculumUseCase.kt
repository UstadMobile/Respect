package world.respect.app.domain

class ValidateCurriculumUseCase {

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_ID = "id"
        const val FIELD_DESCRIPTION = "description"
        const val MIN_NAME_LENGTH = 1
        const val MIN_ID_LENGTH = 1
        const val MIN_DESCRIPTION_LENGTH = 1
        const val MAX_NAME_LENGTH = 100
        const val MAX_ID_LENGTH = 50
        const val MAX_DESCRIPTION_LENGTH = 1000
    }

    data class CurriculumData(
        val name: String,
        val id: String,
        val description: String
    )

    data class ValidationError(
        val field: String,
        val message: String
    )

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<ValidationError>
    ) {
        companion object {
            fun success() = ValidationResult(
                isValid = true,
                errors = emptyList()
            )

            fun failure(errors: List<ValidationError>) = ValidationResult(
                isValid = false,
                errors = errors
            )
        }
    }

    operator fun invoke(curriculumData: CurriculumData): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        val nameValue = curriculumData.name
        if (nameValue.isBlank()) {
            errors.add(
                ValidationError(
                    field = FIELD_NAME,
                    message = "Name is required"
                )
            )
        } else if (nameValue.length < MIN_NAME_LENGTH) {
            errors.add(
                ValidationError(
                    field = FIELD_NAME,
                    message = "Name must be at least $MIN_NAME_LENGTH character"
                )
            )
        } else if (nameValue.length > MAX_NAME_LENGTH) {
            errors.add(
                ValidationError(
                    field = FIELD_NAME,
                    message = "Name must not exceed $MAX_NAME_LENGTH characters"
                )
            )
        }

        // Validate ID
        val idValue = curriculumData.id
        if (idValue.isBlank()) {
            errors.add(
                ValidationError(
                    field = FIELD_ID,
                    message = "ID is required"
                )
            )
        } else if (idValue.length < MIN_ID_LENGTH) {
            errors.add(
                ValidationError(
                    field = FIELD_ID,
                    message = "ID must be at least $MIN_ID_LENGTH character"
                )
            )
        } else if (idValue.length > MAX_ID_LENGTH) {
            errors.add(
                ValidationError(
                    field = FIELD_ID,
                    message = "ID must not exceed $MAX_ID_LENGTH characters"
                )
            )
        }

        // Validate description
        val descriptionValue = curriculumData.description
        if (descriptionValue.isBlank()) {
            errors.add(
                ValidationError(
                    field = FIELD_DESCRIPTION,
                    message = "Description is required"
                )
            )
        } else if (descriptionValue.length < MIN_DESCRIPTION_LENGTH) {
            errors.add(
                ValidationError(
                    field = FIELD_DESCRIPTION,
                    message = "Description must be at least $MIN_DESCRIPTION_LENGTH character"
                )
            )
        } else if (descriptionValue.length > MAX_DESCRIPTION_LENGTH) {
            errors.add(
                ValidationError(
                    field = FIELD_DESCRIPTION,
                    message = "Description must not exceed $MAX_DESCRIPTION_LENGTH characters"
                )
            )
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(errors)
        }
    }
}