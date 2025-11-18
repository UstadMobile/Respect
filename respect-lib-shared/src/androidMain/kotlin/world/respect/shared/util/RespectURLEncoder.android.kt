package world.respect.shared.util

actual class RespectURLEncoder {
    actual companion object {
        actual fun encodeUTF8(text: String): String {
            return java.net.URLEncoder.encode(text, "UTF-8")

        }

        actual fun decodeUTF8(text: String): String {
            return java.net.URLDecoder.decode(text, "UTF-8")
        }
    }
}