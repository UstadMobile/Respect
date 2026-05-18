package world.respect.lib.xapi.exceptions

class XapiBadRequestException(
    override val message: String,
    override val cause: Throwable? = null,
) : XapiException(message, cause, 400)