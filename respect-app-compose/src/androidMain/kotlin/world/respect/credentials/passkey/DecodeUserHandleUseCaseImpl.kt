package world.respect.credentials.passkey

import io.ktor.http.Url
import world.respect.credentials.passkey.request.DecodeUserHandleUseCase
import world.respect.shared.util.base64StringToByteArray
import java.nio.ByteBuffer


class DecodeUserHandleUseCaseImpl : DecodeUserHandleUseCase {
    override operator fun invoke(
        encodedHandle: String
    ): Pair<Url, Long> {
        val decodedBytes = encodedHandle.base64StringToByteArray()
        val byteBuffer = ByteBuffer.wrap(decodedBytes)

        val uid = byteBuffer.long

        val bytesToRead = decodedBytes.size - 8
        val byteArray = ByteArray(bytesToRead)
        byteBuffer.get(byteArray)

        val learningSpaceUrl = byteArray.decodeToString()

        return Pair(Url(learningSpaceUrl), uid)
    }
}
