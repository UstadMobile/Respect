package world.respect.shared.domain.report.model

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import org.jetbrains.compose.resources.StringResource
import world.respect.shared.generated.resources.Res

object StringResourceSerializer : KSerializer<StringResource> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringResource", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: StringResource) {
        encoder.encodeString(value.key)
    }

    override fun deserialize(decoder: Decoder): StringResource {
        val key = decoder.decodeString()
        return try {
            Res.string::class.members
                .first { it.name == key }
                .call(Res.string) as StringResource
        } catch (e: Exception) {
            throw SerializationException("Unknown StringResource key: $key")
        }
    }
}