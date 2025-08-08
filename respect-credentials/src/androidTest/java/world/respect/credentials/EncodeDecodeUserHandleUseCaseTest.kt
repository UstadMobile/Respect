package world.respect.credentials

import io.ktor.http.Url
import world.respect.credentials.passkey.DecodeUserHandleUseCaseImpl
import world.respect.credentials.passkey.EncodeUserHandleUseCaseImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EncodeDecodeUserHandleUseCaseTest {
    val decodeUseCase = DecodeUserHandleUseCaseImpl()
    private fun returnEncodeString(uid: Long, url: String): String {
        val learningSpace = Url(url)
        val encodeUseCase = EncodeUserHandleUseCaseImpl(learningSpace)
        return encodeUseCase(uid)
    }

    @Test
    fun givenPersonUidAndLearningSpace_whenEncodedAndThenDecoded_thenShouldReturnSameValues() {
        val originalUid = 727860624594890752
        val learningSpaceUrl ="http://192.168.1.35:8087/"
        val encoded =returnEncodeString(originalUid,learningSpaceUrl)
        val (decodedLearningSpace, decodedUid) = decodeUseCase(encoded)
        assertEquals(originalUid, decodedUid)
        assertEquals(
            learningSpaceUrl,
            decodedLearningSpace.toString()
        )
    }

    @Test
    fun givenPersonUidAndLiveUrl_whenEncodedAndThenDecoded_thenShouldReturnSameValues() {
        val originalUid = 7272232323
        val learningSpaceUrl="http://learning.tree.com"
        val encoded =returnEncodeString(originalUid,learningSpaceUrl)

        val (decodedLearningSpace, decodedUid) = decodeUseCase(encoded)
        assertEquals(originalUid, decodedUid)

        assertEquals(
            learningSpaceUrl,
            decodedLearningSpace.toString()
        )
    }
    @Test
    fun givenTooLongLearningSpaceUrl_whenEncoded_thenShouldThrowIllegalArgumentException() {
        val longUrl = "http://the.learningspaceurl.istoolong.ShouldThrowIllegalArgumentException.com"
        val exception = assertFailsWith<IllegalArgumentException> {
            returnEncodeString(123456789L, longUrl)
        }
        exception.message?.let { assertTrue(it.contains("URL is too long")) }
    }
}
