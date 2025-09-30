package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.PersonPasskeyEntity
import world.respect.datalayer.school.model.PersonPasskey

fun PersonPasskey.asEntity(
    uidNumberMapper: UidNumberMapper,
): PersonPasskeyEntity {
    return PersonPasskeyEntity(
        ppPersonUid = uidNumberMapper(personGuid),
        personPasskeyUid = personPasskeyUid,
        ppLastModified = lastModified,
        ppStored = stored,
        ppAttestationObj = attestationObj,
        ppClientDataJson = clientDataJson,
        ppOriginString = originString,
        ppId = id,
        ppChallengeString = challengeString,
        ppPublicKey = publicKey,
        ppDeviceName = deviceName,
        ppTimeCreated = timeCreated,
        isRevoked = if (isRevoked) PersonPasskeyEntity.REVOKED else PersonPasskeyEntity.NOT_REVOKED,
        ppAaguid = aaguid ?: "",
        ppProviderName = providerName ?: ""
    )
}

fun PersonPasskeyEntity.asModel(
    personGuid: String,
): PersonPasskey {
    return PersonPasskey(
        personGuid = personGuid,
        personPasskeyUid = personPasskeyUid,
        lastModified = ppLastModified,
        stored = ppStored,
        attestationObj = ppAttestationObj,
        clientDataJson = ppClientDataJson,
        originString = ppOriginString,
        id = ppId,
        challengeString = ppChallengeString,
        publicKey = ppPublicKey,
        isRevoked = isRevoked == PersonPasskeyEntity.REVOKED,
        deviceName = ppDeviceName,
        timeCreated = ppTimeCreated,
        aaguid = ppAaguid,
        providerName = ppProviderName
    )
}
