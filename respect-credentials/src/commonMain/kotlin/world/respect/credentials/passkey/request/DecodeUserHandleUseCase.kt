package world.respect.credentials.passkey.request

import io.ktor.http.Url


/**
 * Decode a user handle encoded by EncodeUserHandleUseCase - see EncodeUserHandleUseCase
 */
interface DecodeUserHandleUseCase {

    operator fun invoke(encodedHandle: String): Pair<Url, Long>

}