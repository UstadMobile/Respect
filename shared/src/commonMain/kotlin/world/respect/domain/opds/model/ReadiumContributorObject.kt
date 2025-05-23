package world.respect.domain.opds.model

import kotlinx.serialization.Serializable
import world.respect.domain.opds.serialization.SingleItemToListTransformer
import world.respect.domain.opds.serialization.StringOrObjectSerializer
import world.respect.domain.opds.serialization.StringValue
import world.respect.domain.opds.serialization.StringValueSerializer

/**
 * Represents a contributor (author, translator, etc.). As per the schema, this can be an object,
 * single string value, list of strings, or list of contributor objects. Fields of the
 * ReadiumContributor type should use a List Kotlin type and use the
 * ReadiumContributorSingleItemToListTransformer serializer to convert single items into a single
 * item list.
 *
 * For reference, see the schema:
 * https://github.com/readium/webpub-manifest/blob/master/schema/contributor.schema.json
 */
@Serializable(with = ReadiumContributorSerializer::class)
sealed class ReadiumContributor

@Serializable
data class ReadiumContributorObject(
    val name: String,
    val identifier: String? = null,
    val sortAs: String? = null,
    val links: List<OpdsLink>? = null,
): ReadiumContributor()

@Serializable(with = ReadiumContributorStringValueSerializer::class)
data class ReadiumContributorStringValue(override val value: String): ReadiumContributor(), StringValue

object ReadiumContributorStringValueSerializer : StringValueSerializer<ReadiumContributorStringValue>(
    serialName = "respect.world.OpdsContributorStringValue",
    stringToValue = { ReadiumContributorStringValue(it) }
)

object ReadiumContributorSerializer: StringOrObjectSerializer<ReadiumContributor>(
    ReadiumContributor::class,
    primitiveSerializer = ReadiumContributorStringValue.serializer(),
    objectSerializer = ReadiumContributorObject.serializer()
)

object ReadiumContributorSingleItemToListTransformer: SingleItemToListTransformer<ReadiumContributor>(
    ReadiumContributor.serializer()
)
