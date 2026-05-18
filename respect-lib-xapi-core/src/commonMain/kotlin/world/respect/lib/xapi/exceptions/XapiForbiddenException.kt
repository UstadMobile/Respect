package world.respect.lib.xapi.exceptions

class XapiForbiddenException(
    message: String,
    cause: Throwable? = null,
): XapiException(message, cause, 403)
