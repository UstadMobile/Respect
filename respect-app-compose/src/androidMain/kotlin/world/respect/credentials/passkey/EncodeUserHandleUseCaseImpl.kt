package world.respect.credentials.passkey

import io.ktor.http.Url
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
import world.respect.credentials.passkey.request.EncodeUserHandleUseCase
import java.nio.ByteBuffer


class EncodeUserHandleUseCaseImpl(
    val url: Url
) : EncodeUserHandleUseCase {

    override fun invoke(
        personPasskeyUid: Long
    ): String {
        val stringToEncode = url.toString()
        val stringBytes = stringToEncode.toByteArray()

        if (stringBytes.size > 55) {
            throw IllegalArgumentException("URL is too long")
        }

        val byteBuffer = ByteBuffer.allocate(8 + stringBytes.size)
        byteBuffer.putLong(personPasskeyUid)
        byteBuffer.put(stringBytes)

        return byteBuffer.array().encodeBase64()
    }
}