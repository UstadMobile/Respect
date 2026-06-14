package world.respect.xapi.ipc.shared.messages.ext

import android.os.Bundle
import io.ktor.http.Headers
import io.ktor.util.StringValues
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys

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

fun <T: Any> Bundle.toDataLoadState(
    json: Json,
    deserializer: DeserializationStrategy<T>
): DataLoadState<T> {
    val status = getInt(XapiIpcKeys.KEY_STATUS_CODE)
    val metaInfo = DataLoadMetaInfo(
        headers = Headers.build {
            this@toDataLoadState.getStringValues(XapiIpcKeys.KEY_HEADERS)?.also { stringVals ->
                appendAll(stringVals)
            }
        }
    )

    return when(status) {
        200 -> {
            DataReadyState(
                data = getDeserialized(
                    key = XapiIpcKeys.KEY_BODY,
                    json = json,
                    deserializer = deserializer,
                ) ?: throw IllegalStateException("200 response has no body"),
                metaInfo = metaInfo,
            )
        }

        302, 404 -> {
            NoDataLoadedState(
                reason = NoDataLoadedState.Reason.forStatusCode(status),
                metaInfo = metaInfo,
            )
        }

        else -> {
            DataErrorResult(
                error = IllegalStateException()
            )
        }
    }
}
