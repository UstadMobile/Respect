package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.db.school.entities.SchoolConfigSettingEntity
import world.respect.datalayer.school.ext.foldToFlag
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.SchoolConfigSetting


fun SchoolConfigSetting.asEntity(): SchoolConfigSettingEntity {
    return SchoolConfigSettingEntity(
        scsKey = key,
        scsValue = value,
        scsStatus = status,
        scsLastModified = lastModified,
        scsStored = stored,
        scsCanReadFlags = canRead.foldToFlag(),
        scsCanWriteFlags = canWrite.foldToFlag(),
        scsAnonCanRead = canRead.contains(null),
    )
}

fun SchoolConfigSettingEntity.asModel(): SchoolConfigSetting {
    return SchoolConfigSetting(
        key = scsKey,
        value = scsValue,
        status = scsStatus,
        lastModified = scsLastModified,
        stored = scsStored,
        canRead = PersonRoleEnum.unfoldFromFlag(scsCanReadFlags),
        canWrite = PersonRoleEnum.unfoldFromFlag(scsCanWriteFlags)
    )
}
