package world.respect.credentials

import junit.framework.TestCase.assertEquals
import passkey.DecodeUserHandleUseCaseImpl
import passkey.EncodeUserHandleUseCaseImpl
import kotlin.test.Test

class EncodeDecodeUserHandleUseCaseTest {
    val decodeUseCase = DecodeUserHandleUseCaseImpl()
    private fun returnEncodeString(uid: Long): String {
        val encodeUseCase = EncodeUserHandleUseCaseImpl()
        return encodeUseCase(uid)
    }

    @Test
    fun givenPersonUidAndLearningSpace_whenEncodedAndThenDecoded_thenShouldReturnSameValues() {
        val originalUid = 727860624594890752
        val encoded =returnEncodeString(originalUid)
        val decodedUid = decodeUseCase(encoded)
        assertEquals(originalUid, decodedUid)
        assertEquals(
            originalUid,
            decodedUid
        )
    }

    @Test
    fun givenPersonUidAndLiveUrl_whenEncodedAndThenDecoded_thenShouldReturnSameValues() {
        val originalUid = 7272232323
        val encoded =returnEncodeString(originalUid)

        val decodedUid = decodeUseCase(encoded)
        assertEquals(originalUid, decodedUid)

        assertEquals(
            originalUid,
            decodedUid
        )
    }
}