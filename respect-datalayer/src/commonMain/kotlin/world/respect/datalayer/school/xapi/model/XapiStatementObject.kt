package world.respect.datalayer.school.xapi.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import world.respect.lib.serializers.SingleItemToListTransformer

/**
 * As per the xAPI spec, can be an Activity, Actor (Agent or Group), or a statement reference.
 *
 * Statement objects look like this:
 *   {
 *      id : "http://...",
 *      objectType: "Activity|Agent|Group|StatementRef"
 *      definition: {
 *         ... may be present if an Activity
 *      }
 *   }
 * So we need to have four different types of XapiStatementObject so the serializer can look at the
 * objectType property to determine the type of the definition object. This sealed interface
 * represents the valid types for the object property on an xAPI statement.
 *
 * When the objectType is Agent or Group, then there is no id or definition property, the XapiAgent
 * and XapiGroup entities are defined as implementing the sealed XapiStatementObject sealed
 * interface themselves.
 */
@Serializable(with = XapiStatementObjectSerializer::class)
sealed interface XapiStatementObject {
    val objectType: XapiObjectType?
}

@Serializable
data class XapiActivityStatementObject(
    override val objectType: XapiObjectType? = null,
    val id: String,
    val definition: XapiActivity? = null,
): XapiStatementObject


object XapiActivityStatementObjectListSerializer: SingleItemToListTransformer<XapiStatementObject>(
    XapiStatementObject.serializer()
)


/**
 * https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/json.md#content-based-polymorphic-deserialization
 */
object XapiStatementObjectSerializer: JsonContentPolymorphicSerializer<XapiStatementObject>(
    XapiStatementObject::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<XapiStatementObject> {

        val objectType = element.jsonObject["objectType"]
            ?.jsonPrimitive?.takeIf { it !is JsonNull }?.content?.let { XapiObjectType.valueOf(it) }
            ?: XapiObjectType.Activity

        return when(objectType) {
            XapiObjectType.Activity -> XapiActivityStatementObject.serializer()
            XapiObjectType.Agent -> XapiAgent.serializer()
            XapiObjectType.Group -> XapiGroup.serializer()
            XapiObjectType.StatementRef -> XapiStatementRef.serializer()
            XapiObjectType.SubStatement -> XapiStatement.serializer()
            else -> throw XapiException(400, "Statement object type invalid")
        }
    }
}
