package world.respect.shared.domain.account.passkey

import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.request.GetAaguidAndProvider
import world.respect.credentials.passkey.model.AaguidProviderData
import world.respect.credentials.passkey.model.PasskeyProviderInfo
import java.nio.ByteBuffer
import java.util.UUID

//https://web.dev/articles/webauthn-aaguid
class GetAaguidAndProviderImpl(
    val json: Json,
    val loadAaguidJsonUseCase: LoadAaguidJsonUseCase
): GetAaguidAndProvider {
    override fun invoke(authenticatorData: String): PasskeyProviderInfo {
        val authenticatorDataByteArray = authenticatorData.decodeBase64Bytes()

        val aaguidOffset = 37

        val aaguidBytes = authenticatorDataByteArray.copyOfRange(aaguidOffset, aaguidOffset + 16)
        val buffer = ByteBuffer.wrap(aaguidBytes)
        val mostSignificantBits = buffer.long
        val leastSignificantBits = buffer.long

        val aaguidUuid = UUID(mostSignificantBits, leastSignificantBits)
        val resourceText = loadAaguidJsonUseCase()
        val aaguidInfo: AaguidProviderData = json.decodeFromString(resourceText?:"")

        val info = aaguidInfo[aaguidUuid.toString()]
        return if (info==null){
            PasskeyProviderInfo(aaguid = aaguidUuid, name = "Unknown")
        }else{
            PasskeyProviderInfo(aaguid = aaguidUuid, name = info.name?:"")
        }

    }
}