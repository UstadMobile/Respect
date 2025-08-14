package passkey

import world.respect.credentials.passkey.request.DecodeUserHandleUseCase
import world.respect.shared.util.base64StringToByteArray
import java.nio.ByteBuffer


class DecodeUserHandleUseCaseImpl : DecodeUserHandleUseCase {
    override operator fun invoke(
        encodedHandle: String
    ): Long {
        val decodedBytes = encodedHandle.base64StringToByteArray()
        val byteBuffer = ByteBuffer.wrap(decodedBytes)
        return byteBuffer.long
    }
}
