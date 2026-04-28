package world.respect.lib.xapi.model

class XapiException(
    val responseCode: Int,
    message: String,
    cause: Throwable? = null
): Exception(message, cause)
