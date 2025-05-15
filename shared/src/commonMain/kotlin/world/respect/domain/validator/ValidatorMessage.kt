package world.respect.domain.validator

data class ValidatorMessage(
    val isError: Boolean,
    val sourceUri: String,
    val message: String,
)
