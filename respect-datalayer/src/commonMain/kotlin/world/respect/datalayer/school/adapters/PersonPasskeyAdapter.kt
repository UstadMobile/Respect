package world.respect.datalayer.school.adapters

import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.credentials.passkey.model.ClientDataJSON
import world.respect.datalayer.school.model.PersonPasskey
import kotlin.time.Clock

fun AuthenticationResponseJSON.toPersonPasskey(
    json : Json,
    personGuid: String,
    deviceName: String,
): PersonPasskey{
    val clientDataJSONBase64 = this.response.clientDataJSON
    val decodedBytes = clientDataJSONBase64.decodeBase64Bytes()
    val clientDataJson = json.decodeFromString<ClientDataJSON>(
        decodedBytes.decodeToString()
    )

    val timeNow = Clock.System.now()

    return PersonPasskey(
        personGuid = personGuid,
        lastModified = timeNow,
        stored = timeNow,
        attestationObj = response.attestationObject,
        clientDataJson = response.clientDataJSON,
        originString = clientDataJson.origin,
        id = id,
        challengeString = clientDataJson.challenge,
        publicKey = response.publicKey,
        deviceName = deviceName,
        isRevoked = false,
    )
}