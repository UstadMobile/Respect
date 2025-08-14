package passkey

import io.ktor.util.encodeBase64
import world.respect.credentials.passkey.request.EncodeUserHandleUseCase
import java.nio.ByteBuffer


class EncodeUserHandleUseCaseImpl() : EncodeUserHandleUseCase {

    override fun invoke(
        personPasskeyUid: Long
    ): String {
        val byteBuffer = ByteBuffer.allocate(Long.SIZE_BYTES)
        byteBuffer.putLong(personPasskeyUid)
        return byteBuffer.array().encodeBase64()
    }
}