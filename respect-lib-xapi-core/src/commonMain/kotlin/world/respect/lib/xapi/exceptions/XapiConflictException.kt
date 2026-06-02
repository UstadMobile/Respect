package world.respect.lib.xapi.exceptions

class XapiConflictException(
    message: String = "Conflict",
    cause: Throwable? = null,
): XapiException(
    message = message,
    cause = cause,
    httpStatusCode = 409
)
