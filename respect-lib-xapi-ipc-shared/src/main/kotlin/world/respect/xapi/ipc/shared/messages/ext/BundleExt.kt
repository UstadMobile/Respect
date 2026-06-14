package world.respect.xapi.ipc.shared.messages.ext

import android.os.Bundle
import io.ktor.util.StringValues
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

fun Bundle.putUuid(key: String, uuid: Uuid) {
    putString(key, uuid.toString())
}

fun Bundle.putUuidIfNotNull(key: String, uuid: Uuid?) {
    uuid?.also { putUuid(key, it) }
}

fun Bundle.getUuidOrNull(key: String): Uuid? {
    return getString(key, null)?.let { Uuid.parse(it) }
}

fun Bundle.putStringIfNotNull(key: String, value: String?) {
    value?.also { putString(key, value) }
}

fun Bundle.putLongIfNotNull(key: String, value: Long?) {
    value?.also { putLong(key ,value) }
}

fun Bundle.getLongOrNull(key: String): Long? {
    return if(containsKey(key))
        getLong(key)
    else
        null
}

fun Bundle.putIntIfNotNull(key: String, value: Int?) {
    value?.also { putInt(key ,value) }
}

fun Bundle.getIntOrNull(key: String): Int? {
    return if(containsKey(key))
        getInt(key)
    else
        null
}

fun <T: Any> Bundle.putSerialized(
    key: String,
    json: Json,
    serializer: SerializationStrategy<T>,
    value: T,
) {
    putString(key, json.encodeToString(serializer, value))
}

fun <T: Any> Bundle.getDeserialized(
    key: String,
    json: Json,
    deserializer: DeserializationStrategy<T>
): T? {
    return getString(key)?.let {
        json.decodeFromString(deserializer, it)
    }
}

private const val SUFFIX_STR_VALS_CASE_INSENSITIVE = "_caseInsensitive"

fun Bundle.putStringValues(
    key: String,
    value: StringValues
) {
    putBundle(key, value.toBundle())
    putBoolean(key + SUFFIX_STR_VALS_CASE_INSENSITIVE, value.caseInsensitiveName)
}

fun Bundle.getStringValues(
    key: String
): StringValues? {
    val bundle = getBundle(key) ?: return null
    val caseInsensitive = getBoolean(key + SUFFIX_STR_VALS_CASE_INSENSITIVE)
    return BundleStringValues(bundle, caseInsensitive)
}
