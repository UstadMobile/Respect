package world.respect.lib.dataloadstate

import kotlin.time.Instant

data class ETagAndLastModified(
    val etag: String?,
    val lastModified: Instant?
)