package world.respect.shared.domain.account.passkey

import io.ktor.http.Url
import world.respect.credentials.passkey.RespectUserHandle
import world.respect.credentials.passkey.request.DecodeUserHandleUseCase
import world.respect.shared.util.base64StringToByteArray
import java.nio.ByteBuffer


class DecodeUserHandleUseCaseImpl : DecodeUserHandleUseCase {

    override operator fun invoke(
        encodedHandle: String
    ): RespectUserHandle {
        val decodedBytes = encodedHandle.base64StringToByteArray()
        val byteBuffer = ByteBuffer.wrap(decodedBytes)
        val personPasskeyUid = byteBuffer.long
        val urlLen = byteBuffer.get()
        val urlBytes = ByteArray(urlLen.toInt())
        byteBuffer.get(urlBytes)
        val schoolUrl = urlBytes.decodeToString()

        return RespectUserHandle(
            personPasskeyUid = personPasskeyUid,
            schoolUrl = Url(schoolUrl),
        )
    }

}
