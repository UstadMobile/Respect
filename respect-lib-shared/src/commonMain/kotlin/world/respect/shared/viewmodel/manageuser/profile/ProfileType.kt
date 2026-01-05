package world.respect.shared.viewmodel.manageuser.profile

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable(with = ProfileTypeSerializer::class)
enum class ProfileType(val value: String) {

    PARENT("parent"),

    //Child profile comes AFTER creation of a Parent profile (student not registering directly)
    CHILD("child"),

    //Student profile is used when a student is registering directly (not via the parent)
    STUDENT("student"),

    TEACHER("teacher");

    companion object {

        fun fromValue(value: String): ProfileType {
            return ProfileType.entries.first { it.value == value }
        }
    }

}

object ProfileTypeSerializer: KSerializer<ProfileType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.ProfileTypeEnum", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: ProfileType
    ) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): ProfileType {
        val stringVal = decoder.decodeString()
        return ProfileType.entries.first { it.value == stringVal }
    }

}
