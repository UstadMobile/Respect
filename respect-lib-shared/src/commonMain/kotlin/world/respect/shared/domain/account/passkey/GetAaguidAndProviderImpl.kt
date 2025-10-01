package world.respect.shared.domain.account.passkey

import io.github.aakira.napier.Napier
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.request.GetAaguidAndProvider
import world.respect.credentials.passkey.model.PasskeyProviderInfo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

//https://web.dev/articles/webauthn-aaguid
class GetAaguidAndProviderImpl(
    val json: Json,
    val loadAaguidJsonUseCase: LoadAaguidJsonUseCase
): GetAaguidAndProvider {

    /**
     * @param authenticatorData the Base64 encoded Authenticator data as per
     *        AuthenticatorAssertionResponseJSON.authenticatorData
     */
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(authenticatorData: String): PasskeyProviderInfo {
        return try {
            val authenticatorDataByteArray = authenticatorData.decodeBase64Bytes()

            val aaguidUuid = Uuid.fromByteArray(
                authenticatorDataByteArray.copyOfRange(AAGUID_OFFSET, AAGUID_OFFSET + UUID_LEN)
            )

            val aaguidInfo = loadAaguidJsonUseCase()
                ?: return PasskeyProviderInfo(aaguid = aaguidUuid, name = "Unknown", icon_dark = null, icon_light = null)

            aaguidInfo[aaguidUuid.toString()]?.let {
                PasskeyProviderInfo(aaguid = aaguidUuid, name = it.name ?: "", icon_dark = it.icon_dark, icon_light = it.icon_light)
            } ?: PasskeyProviderInfo(aaguid = aaguidUuid, name = "Unknown", icon_dark = null, icon_light = null)
        }catch (t: Throwable) {
            Napier.w("Exception attempting to get aaguid data for passkey:", t)
            PasskeyProviderInfo(aaguid = Uuid.NIL, name = "Unknown", icon_dark = null, icon_light = null)
        }
    }


    companion object {

        /**
         * The authenticator data as per https://web.dev/articles/webauthn-aaguid contains:
         * 32 bytes: rpId hash
         * 1 byte: flags
         * 4 bytes: counter
         *
         * 16 bytes: aaguid
         * Hence the AAGUID starts at 37 (32 + 1 + 4)
         *
         */
        const val AAGUID_OFFSET = 37

        const val UUID_LEN = 16

    }
}