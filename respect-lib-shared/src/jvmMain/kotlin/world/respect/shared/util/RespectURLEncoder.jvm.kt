package world.respect.shared.util

/**
 * External JS function to encode URI
 */
external fun encodeURI(uri: String?): String

external fun encodeURIComponent(uriComponent: String?): String

/**
 * External JS function to decode URI
 */
external fun decodeURI(uri: String?): String

external fun decodeURIComponent(uriComponent: String?): String
actual class RespectURLEncoder {
    actual companion object {
        actual fun encodeUTF8(text: String): String {
            return encodeURIComponent(text)

        }

        actual fun decodeUTF8(text: String): String {
            return decodeURIComponent(text)
        }
    }
}