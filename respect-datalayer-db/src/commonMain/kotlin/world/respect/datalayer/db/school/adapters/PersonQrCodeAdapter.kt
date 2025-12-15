package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.PersonQrCodeEntity
import world.respect.datalayer.school.model.PersonQrCode

fun PersonQrCode.asEntity(
    uidNumberMapper: UidNumberMapper
): PersonQrCodeEntity {
    return PersonQrCodeEntity(
        pqrGuid = personGuid,
        pqrGuidNum =  uidNumberMapper(personGuid),
        pqrLastModified = lastModified,
        pqrStored = stored,
        pqrQrCodeUrl = qrCodeUrl
    )
}

fun PersonQrCodeEntity.asModel(): PersonQrCode {
    return PersonQrCode(
        personGuid = pqrGuid,
        qrCodeUrl = pqrQrCodeUrl,
        lastModified = pqrLastModified,
        stored = pqrStored,
    )
}
