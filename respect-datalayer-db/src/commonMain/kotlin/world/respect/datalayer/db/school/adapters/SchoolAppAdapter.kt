package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.SchoolAppEntity
import world.respect.datalayer.school.model.SchoolApp

fun SchoolAppEntity.toModel() = SchoolApp(
    uid = saUid,
    appManifestUrl = saManifestUrl,
    status = saStatus,
    lastModified = saLastModified,
    stored = saStored,
)

fun SchoolApp.toEntity(
    uidNumberMapper: UidNumberMapper
) = SchoolAppEntity(
    saUid = uid,
    saUidNum = uidNumberMapper(uid),
    saManifestUrl = appManifestUrl,
    saStatus = status,
    saLastModified = lastModified,
    saStored = stored,
)

