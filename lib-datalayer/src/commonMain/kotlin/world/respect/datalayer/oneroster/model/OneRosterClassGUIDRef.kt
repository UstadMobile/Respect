package world.respect.datalayer.oneroster.model

import com.eygraber.uri.Uri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * As per OneRoster spec 6.1.6
 * https://www.imsglobal.org/sites/default/files/spec/oneroster/v1p2/rostering-informationmodel/OneRosterv1p2RosteringService_InfoModelv1p0.html#Data_ClassGUIDRef
 */
@Serializable
class OneRosterClassGUIDRef(
    @Serializable(with = UriSerializer::class)
    override val href: Uri,
    override val sourcedId: String,
    val type: ClassGUIDRefTypeEnum = ClassGUIDRefTypeEnum.CLASS,
): OneRosterGUIDRef {

    enum class ClassGUIDRefTypeEnum(val value: String) {
        CLASS("class")
    }

}
// Custom serializer for android.net.Uri since kotlinx.serialization does not support Uri by default.
// Based on stackoverflow example
// https://stackoverflow.com/questions/22769672/serialize-android-net-uri-object
object UriSerializer : KSerializer<Uri> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Uri {
        return Uri.parse(decoder.decodeString())
    }
}