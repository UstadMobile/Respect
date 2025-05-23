package world.respect.domain.opds.model

import com.eygraber.uri.Uri
import kotlinx.serialization.Serializable
import world.respect.domain.opds.serialization.SingleItemToListTransformer
import world.respect.domain.opds.serialization.StringOrObjectSerializer
import world.respect.domain.opds.serialization.StringValue
import world.respect.domain.opds.serialization.StringValueSerializer

/**
 * Schema: https://readium.org/webpub-manifest/schema/subject.schema.json
 */
@Serializable(with = ReadiumSubjectSerializer::class)
sealed class ReadiumSubject

@Serializable(with = ReadiumSubjectStringValueSerializer::class)
data class ReadiumSubjectStringValue(override val value: String): ReadiumSubject(), StringValue

object ReadiumSubjectStringValueSerializer: StringValueSerializer<ReadiumSubjectStringValue>(
    serialName = "ReadiumSubjectStringValue", stringToValue = { ReadiumSubjectStringValue(it) }
)

/**
 * Schema: https://readium.org/webpub-manifest/schema/subject-object.schema.json
 */
@Serializable
data class ReadiumSubjectObject(
    val name: LangMap,
    val sortAs: String? = null,
    val code: String? = null,
    val scheme: Uri? = null,
    val links: List<OpdsLink>? = null,
): ReadiumSubject()

object ReadiumSubjectSerializer: StringOrObjectSerializer<ReadiumSubject>(
    ReadiumSubject::class,
    primitiveSerializer = ReadiumSubjectStringValue.serializer(),
    objectSerializer = ReadiumSubjectObject.serializer()
)

object ReadiumSubjectToListTransformer: SingleItemToListTransformer<ReadiumSubject>(
    ReadiumSubject.serializer()
)
