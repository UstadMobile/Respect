package world.respect.libutil.util.throwable

/**
 * Some exceptions map to a particular http status code This interface makes it easier for http
 * server and client components to handle exceptions e.g. an http server's exception handling can
 * simply catch the exception and then set the status code directly.
 */
interface ExceptionWithHttpStatusCode {

    val statusCode: Int

}

class ExceptionWithHttpStatusCodeWrapper internal constructor(
    cause: Throwable?,
    message: String?,
    override val statusCode: Int
): Exception(message, cause), ExceptionWithHttpStatusCode

fun Throwable.withHttpStatus(statusCode: Int): Exception {
    return ExceptionWithHttpStatusCodeWrapper(this, message, statusCode)
}
