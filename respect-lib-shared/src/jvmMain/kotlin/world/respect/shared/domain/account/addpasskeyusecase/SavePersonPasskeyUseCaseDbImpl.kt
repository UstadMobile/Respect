package world.respect.shared.domain.account.addpasskeyusecase

import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.model.ClientDataJSON
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.opds.entities.PersonPasskeyEntity
import kotlin.time.Clock

class SavePersonPasskeyUseCaseDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val json: Json,
) : SavePersonPasskeyUseCase {

    override suspend fun invoke(request: SavePersonPasskeyUseCase.Request) {
        val clientDataJSONBase64 = request.passkeyWebAuthNResponse.response.clientDataJSON
        val decodedBytes = clientDataJSONBase64.decodeBase64Bytes()
        val clientDataJson = json.decodeFromString<ClientDataJSON>(
            decodedBytes.decodeToString()
        )

        val timeNow = Clock.System.now()
        val personPasskey = PersonPasskeyEntity(
            ppPersonUid = uidNumberMapper(request.authenticatedUserId.guid),
            ppLastModified = timeNow,
            ppStored = timeNow,
            ppAttestationObj = request.passkeyWebAuthNResponse.response.attestationObject,
            ppClientDataJson = request.passkeyWebAuthNResponse.response.clientDataJSON,
            ppOriginString = clientDataJson.origin,
            ppId = request.passkeyWebAuthNResponse.id,
            ppChallengeString = clientDataJson.challenge,
            ppPublicKey = request.passkeyWebAuthNResponse.response.publicKey,
            ppDeviceName = request.deviceName,
        )

        schoolDb.getPersonPasskeyEntityDao().upsertAsync(listOf(personPasskey))
    }

}