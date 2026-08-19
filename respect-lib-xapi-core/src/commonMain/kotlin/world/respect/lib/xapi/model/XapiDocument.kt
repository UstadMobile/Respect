package world.respect.lib.xapi.model

import kotlin.time.Instant

/**
 * Xapi Document: this interface could wrap different types: e.g. entities on the database that are using a bytearray,
 * an http response, etc.
 *
 * See
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#22-document-resources
 */
interface XapiDocument {

    val id: String

    val type: String

    val updated: Instant

    suspend fun contentAsByteArray(): ByteArray

}