package world.respect.shared.domain.getdeviceinfo

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

interface GetDeviceInfoUseCase {

    @Serializable(with = PlatformSerializer::class)
    enum class Platform(val pName: String) {
        ANDROID("android"), UNKNOWN("unknown");

        companion object {

            fun fromValue(value: String): Platform {
                return entries.first { it.pName == value }
            }

        }
    }

    @Serializable
    data class DeviceInfo(
        val platform: Platform,
        val androidSdkInt: Int,
        val version: String,
        val manufacturer: String?,
        val model: String?,
        val ram: Long,
    )

    operator fun invoke() : DeviceInfo

}

object PlatformSerializer : KSerializer<GetDeviceInfoUseCase.Platform> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.Platform", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: GetDeviceInfoUseCase.Platform
    ) {
        encoder.encodeString(value.pName)
    }

    override fun deserialize(decoder: Decoder): GetDeviceInfoUseCase.Platform {
        return GetDeviceInfoUseCase.Platform.fromValue(decoder.decodeString())
    }
}
