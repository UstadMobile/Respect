package world.respect.lib.xapi.model

import kotlin.time.Instant

class XapiDocumentByteArrayImpl(
    override val type: String,
    override val updated: Instant,
    val contents: ByteArray,
) : XapiDocument {
    override suspend fun contentsAsByteArray(): ByteArray = contents
}