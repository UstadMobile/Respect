package world.respect.datalayer.db.schooldirectory.adapters

import world.respect.datalayer.DataLoadMetaInfo
import world.respect.datalayer.DataReadyState
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryEntity
import world.respect.datalayer.db.shared.adapters.asEntities
import world.respect.datalayer.db.shared.adapters.toModel
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.libxxhash.XXStringHasher

data class SchoolDirectoryEntryEntities(
    val school: SchoolDirectoryEntryEntity,
    val langMapEntities: List<LangMapEntity>,
)

fun DataReadyState<SchoolDirectoryEntry>.toEntities(
    xxStringHasher: XXStringHasher,
): SchoolDirectoryEntryEntities {
    val reUid = xxStringHasher.hash(data.self.toString())
    return SchoolDirectoryEntryEntities(
        school = SchoolDirectoryEntryEntity(
            reUid = reUid,
            reSelf = data.self,
            reXapi = data.xapi,
            reOneRoster = data.oneRoster,
            reLastMod = metaInfo.lastModified,
            reEtag = metaInfo.etag,
            reRespectExt = data.respectExt,
            reRpId = data.rpId,
            reSchoolCode = data.schoolCode,
        ),
        langMapEntities = data.name.asEntities(
            lmeTopParentType = LangMapEntity.TopParentType.RESPECT_SCHOOL_DIRECTORY_ENTRY,
            lmeTopParentUid1 = reUid,
            lmePropType = LangMapEntity.PropType.RESPECT_SCHOOL_DIRECTORY_ENTRY_NAME,
            lmePropFk = 0,
        )
    )
}

fun SchoolDirectoryEntryEntities.toModel() : DataReadyState<SchoolDirectoryEntry> {
    return DataReadyState(
        data = SchoolDirectoryEntry(
            self = school.reSelf,
            xapi = school.reXapi,
            oneRoster = school.reOneRoster,
            respectExt = school.reRespectExt,
            name = langMapEntities.toModel(),
            rpId = school.reRpId,
            schoolCode = school.reSchoolCode,
        ),
        metaInfo = DataLoadMetaInfo(
            lastModified = school.reLastMod,
            etag = school.reEtag,
        )
    )
}
