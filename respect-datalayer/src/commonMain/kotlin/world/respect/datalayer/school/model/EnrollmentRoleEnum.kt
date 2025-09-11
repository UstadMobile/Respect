package world.respect.datalayer.school.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = EnrollmentRoleSerializer::class)
enum class EnrollmentRoleEnum(
    val value: String,
    val flag: Int,
) {

    TEACHER("teacher", 1), STUDENT("student", 2);

    companion object {


        @Suppress("unused") //Reserved for DB usage
        const val TEACHER_FLAG = 1

        @Suppress("unused") //Reserved for DB usage
        const val STUDENT_FLAG = 2

        fun fromValue(value: String): EnrollmentRoleEnum {
            return entries.first { it.value == value }
        }

        @Suppress("unused") //Reserved for DB usage
        fun fromFlag(flag: Int): EnrollmentRoleEnum {
            return entries.first { it.flag == flag }
        }
    }
}

object EnrollmentRoleSerializer: KSerializer<EnrollmentRoleEnum> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.EnrollmentRoleEnum", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: EnrollmentRoleEnum
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): EnrollmentRoleEnum {
        return EnrollmentRoleEnum.fromValue(decoder.decodeString())
    }
}
