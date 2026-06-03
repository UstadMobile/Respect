package world.respect.lib.xapi.exceptions

abstract class XapiException(
    message: String,
    cause: Throwable? = null,
    val httpStatusCode: Int,
) : Exception(message, cause)
