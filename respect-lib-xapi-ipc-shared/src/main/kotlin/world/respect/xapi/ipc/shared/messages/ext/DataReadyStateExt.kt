package world.respect.xapi.ipc.shared.messages.ext

import android.os.Bundle
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.xapi.ext.xapiHttpStatusCodeOrNull
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys

const val STATUS_LOADING = -2

fun <T: Any> DataLoadState<T>.toBundle(
    serializer: SerializationStrategy<T>,
    json: Json,
): Bundle {
    return when(this) {
        is DataReadyState<T> -> {
            Bundle().apply {
                putInt(XapiIpcKeys.KEY_STATUS_CODE, 200)
                putSerialized(
                    key = XapiIpcKeys.KEY_BODY,
                    json = json,
                    serializer = serializer,
                    value = data,
                )
                putBundle(XapiIpcKeys.KEY_HEADERS, metaInfo.toBundle())
            }
        }

        is DataLoadingState<T> -> {
            Bundle().apply {
                putInt(XapiIpcKeys.KEY_STATUS_CODE, STATUS_LOADING)
                putBundle(XapiIpcKeys.KEY_HEADERS, metaInfo.toBundle())
            }
        }

        is DataErrorResult<T> -> {
            Bundle().also {
                it.putInt(XapiIpcKeys.KEY_STATUS_CODE, error.xapiHttpStatusCodeOrNull() ?: 500)
                it.putBundle(XapiIpcKeys.KEY_HEADERS, metaInfo.toBundle())
            }
        }

        is NoDataLoadedState<T> -> {
            Bundle().also {
                it.putBundle(XapiIpcKeys.KEY_HEADERS, metaInfo.toBundle())
                it.putInt(
                    XapiIpcKeys.KEY_STATUS_CODE,
                    if(reason == NoDataLoadedState.Reason.NOT_MODIFIED) {
                        302
                    }else {
                        404
                    }
                )
            }
        }

    }
}