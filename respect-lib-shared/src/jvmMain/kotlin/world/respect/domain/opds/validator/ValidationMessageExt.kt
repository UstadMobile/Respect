package world.respect.domain.opds.validator

import com.networknt.schema.ValidationMessage as SchemaValidatorMessage
import world.respect.domain.validator.ValidatorMessage

fun SchemaValidatorMessage.toValidatorMessage(sourceUri: String) = ValidatorMessage(
    level = ValidatorMessage.Level.ERROR,
    sourceUri = sourceUri,
    message = toString()
)
