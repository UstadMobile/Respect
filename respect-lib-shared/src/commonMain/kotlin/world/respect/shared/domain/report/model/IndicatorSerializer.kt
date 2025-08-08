package world.respect.shared.domain.report.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.total_duration


//TODO NEED MODIFICATION
object IndicatorSerializer : KSerializer<Indicator> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Indicator") {
        element("name", PrimitiveSerialDescriptor("name", PrimitiveKind.STRING))
        element("description", PrimitiveSerialDescriptor("description", PrimitiveKind.STRING))
        element("sql", PrimitiveSerialDescriptor("sql", PrimitiveKind.STRING))
        element("label", PrimitiveSerialDescriptor("label", PrimitiveKind.STRING))
        element("type", PrimitiveSerialDescriptor("type", PrimitiveKind.STRING))
    }

    override fun deserialize(decoder: Decoder): Indicator {
        return when (decoder) {
            is JsonDecoder -> decodeFromJsonElement(decoder.decodeJsonElement())
            else -> DefaultIndicators.list.first()
        }
    }

    private fun decodeFromJsonElement(element: JsonElement): Indicator {
        val jsonObject = element.jsonObject
        val labelString = jsonObject["label"]?.jsonPrimitive?.contentOrNull ?: ""

        // Find matching indicator in DefaultIndicators by label string
        val defaultIndicator = DefaultIndicators.list.firstOrNull { indicator ->
            indicator.label.toString() == labelString
        }

        return Indicator(
            name = jsonObject["name"]?.jsonPrimitive?.contentOrNull ?: "",
            description = jsonObject["description"]?.jsonPrimitive?.contentOrNull ?: "",
            sql = jsonObject["sql"]?.jsonPrimitive?.contentOrNull ?: "",
            label = defaultIndicator?.label ?: Res.string.total_duration,
            type = when (val typeValue = jsonObject["type"]?.jsonPrimitive?.contentOrNull) {
                "DURATION" -> YAxisTypes.DURATION
                "PERCENTAGE" -> YAxisTypes.PERCENTAGE
                else -> YAxisTypes.COUNT
            }
        )
    }

    override fun serialize(encoder: Encoder, value: Indicator) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name ?: "")
            encodeStringElement(descriptor, 1, value.description ?: "")
            encodeStringElement(descriptor, 2, value.sql ?: "")
            encodeStringElement(descriptor, 3, value.label.toString())
            encodeStringElement(descriptor, 4, value.type.name)
        }
    }
}