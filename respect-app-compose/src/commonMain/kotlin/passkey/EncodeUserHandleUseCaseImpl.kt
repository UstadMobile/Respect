package passkey

import io.ktor.util.encodeBase64
import world.respect.credentials.passkey.request.EncodeUserHandleUseCase
import java.nio.ByteBuffer
import kotlin.random.Random


class EncodeUserHandleUseCaseImpl() : EncodeUserHandleUseCase {

    override fun invoke(
        personPasskeyUid: Long
    ): String {
        val shortUid = Random.nextInt(0x10000).toShort()
        val byteBuffer = ByteBuffer.allocate(Long.SIZE_BYTES+ Short.SIZE_BYTES)
        byteBuffer.putLong(personPasskeyUid)
        byteBuffer.putShort(shortUid)
        return byteBuffer.array().encodeBase64()
    }
}