package world.respect.datalayer.school.ext

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.Enrollment.Companion.METADATA_KEY_CLASS_NAME
import world.respect.datalayer.school.model.EnrollmentRoleEnum

fun Enrollment.copyAsApproved(): Enrollment {
    val currentRole = role
    return copy(
        role = when (currentRole) {
            EnrollmentRoleEnum.PENDING_TEACHER -> EnrollmentRoleEnum.TEACHER
            EnrollmentRoleEnum.PENDING_STUDENT -> EnrollmentRoleEnum.STUDENT
            else -> currentRole
        }
    )
}

fun Enrollment.copyWithClassName(
    className: String
): Enrollment {
    return copy(
        metadata = buildJsonObject {
            this@copyWithClassName.metadata?.also {
                putAll(it)
            }

            put(METADATA_KEY_CLASS_NAME, JsonPrimitive(className))
        }
    )
}

fun Enrollment.getClassName(): String? {
    return metadata
        ?.get(METADATA_KEY_CLASS_NAME)
        ?.jsonPrimitive
        ?.contentOrNull
}