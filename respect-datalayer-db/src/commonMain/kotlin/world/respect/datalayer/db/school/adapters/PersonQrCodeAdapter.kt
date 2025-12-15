package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.PersonBadgeEntity
import world.respect.datalayer.school.model.PersonBadge

fun PersonBadge.asEntity(
    uidNumberMapper: UidNumberMapper
): PersonBadgeEntity {
    return PersonBadgeEntity(
        pqrGuid = personGuid,
        pqrGuidNum =  uidNumberMapper(personGuid),
        pqrLastModified = lastModified,
        pqrStored = stored,
        pqrQrCodeUrl = qrCodeUrl
    )
}

fun PersonBadgeEntity.asModel(): PersonBadge {
    return PersonBadge(
        personGuid = pqrGuid,
        qrCodeUrl = pqrQrCodeUrl,
        lastModified = pqrLastModified,
        stored = pqrStored,
    )
}
