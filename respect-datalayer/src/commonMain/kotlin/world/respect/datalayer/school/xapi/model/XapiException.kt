package world.respect.datalayer.school.xapi.model

class XapiException(
    val responseCode: Int,
    message: String,
    cause: Throwable? = null
): Exception(message, cause)
