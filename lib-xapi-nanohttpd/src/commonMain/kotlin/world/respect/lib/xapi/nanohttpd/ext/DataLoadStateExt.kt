package world.respect.lib.xapi.nanohttpd.ext

import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.lib.dataloadstate.throwable.unwrapHttpStatusCode

/**
 * Note: still needs to handle not modified etc
 */
fun <T: Any> DataLoadState<T>.toFixedLengthResponse(
    json: Json,
    serializer: SerializationStrategy<T>
): Response {
    return when(this) {
        is DataReadyState -> {
            val jsonText = json.encodeToString(serializer, this.data)
            newFixedLengthResponse(
                Response.Status.OK, "application/json", jsonText
            )
        }

        is NoDataLoadedState -> {
            when(this.reason) {
                NoDataLoadedState.Reason.NOT_MODIFIED -> {
                    newFixedLengthResponse(Response.Status.NOT_MODIFIED, "text/plain", "")
                }
                NoDataLoadedState.Reason.NOT_FOUND -> {
                    newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found")
                }
            }
        }

        is DataErrorResult -> {
            val statusCode = this.error.unwrapHttpStatusCode() ?: 500
            val status = Response.Status.lookup(statusCode) ?: Response.Status.INTERNAL_ERROR
            newFixedLengthResponse(status, "text/plain", this.error.message ?: "")
        }

        is DataLoadingState -> {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "loading")
        }
    }
}
