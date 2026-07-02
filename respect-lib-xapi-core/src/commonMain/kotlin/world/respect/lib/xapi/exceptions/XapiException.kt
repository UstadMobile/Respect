package world.respect.lib.xapi.exceptions

class XapiException(
    val httpStatusCode: Int,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
