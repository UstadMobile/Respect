package world.respect.domain.opds.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

/**
 * Represents the rel field which can be either a single string or an array of strings.
 * The rel field defines the relationship between a resource and its linked resource.
 *
 * For reference, see the schema: https://readium.org/webpub-manifest/schema/link.schema.json
 */
@Serializable(with = OpdsRelSerializer::class)
sealed class OpdsRel {
    /**
     * Checks if this relation contains the specified value.
     */
    abstract fun contains(value: String): Boolean

    /**
     * A single relation value.
     */
    data class Single(val value: String) : OpdsRel() {
        override fun contains(value: String): Boolean = this.value == value
        override fun toString(): String = value
    }

    /**
     * Multiple relation values.
     */
    data class Multiple(val values: List<String>) : OpdsRel() {
        override fun contains(value: String): Boolean = values.contains(value)
        override fun toString(): String = values.toString()
    }
}


/**
 * Custom serializer for OpdsRel to handle both string and array formats.
 * This allows the rel field to be deserialized from either a string or an array of strings.
 */
object OpdsRelSerializer : KSerializer<OpdsRel> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("OpdsRel")

    override fun serialize(encoder: Encoder, value: OpdsRel) {
        require(encoder is JsonEncoder) { "This serializer can only be used with JSON" }
        val jsonElement = when (value) {
            is OpdsRel.Single -> JsonPrimitive(value.value)
            is OpdsRel.Multiple -> JsonArray(value.values.map { JsonPrimitive(it) })
        }
        encoder.encodeJsonElement(jsonElement)
    }

    override fun deserialize(decoder: Decoder): OpdsRel {
        require(decoder is JsonDecoder) { "This serializer can only be used with JSON" }

        return when (val element = decoder.decodeJsonElement()) {
            is JsonPrimitive -> {
                if (!element.isString) {
                    throw SerializationException("Expected a string for rel")
                }
                OpdsRel.Single(element.content)
            }
            is JsonArray -> {
                val values = element.mapNotNull {
                    (it as? JsonPrimitive)?.takeIf { it.isString }?.content
                }
                OpdsRel.Multiple(values)
            }
            else -> throw SerializationException("Expected a string or array for rel")
        }
    }
}