package world.respect.libutil.util.throwable

class ForbiddenException(
    message: String? = null, cause: Throwable? = null
): Exception(message, cause), ExceptionWithHttpStatusCode {

    override val statusCode = 403

}