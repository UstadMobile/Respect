package world.respect.datalayer.db.schooldirectory.adapters

import androidx.room.Embedded
import androidx.room.Relation
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryEntity
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryLangMapEntity
import world.respect.datalayer.db.shared.adapters.asEntities
import world.respect.datalayer.db.shared.adapters.toIModel
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.libxxhash.XXStringHasher

data class SchoolDirectoryEntryEntities(
    @Embedded
    val school: SchoolDirectoryEntryEntity,

    @Relation(
        parentColumn = "reUid",
        entityColumn = "sdelReUid"
    )
    val langMapEntities: List<SchoolDirectoryEntryLangMapEntity>,
)

fun SchoolDirectoryEntry.toEntities(
    xxStringHasher: XXStringHasher,
): SchoolDirectoryEntryEntities {
    val reUid = xxStringHasher.hash(self.toString())
    return SchoolDirectoryEntryEntities(
        school = SchoolDirectoryEntryEntity(
            reUid = reUid,
            reSelf = self,
            reXapi = xapi,
            reOneRoster = oneRoster,
            reRespectExt = respectExt,
            reRpId = rpId,
            reSchoolCode = schoolCode,
            reLastModified = lastModified,
            reStored = stored,
        ),
        langMapEntities = name.asEntities { lang, region, value ->
            SchoolDirectoryEntryLangMapEntity(
                sdelReUid = reUid,
                sdelLang = lang,
                sdelRegion = region,
                sdelValue = value,
            )
        }
    )
}

fun SchoolDirectoryEntryEntities.toModel() : SchoolDirectoryEntry {
    return SchoolDirectoryEntry(
        self = school.reSelf,
        xapi = school.reXapi,
        oneRoster = school.reOneRoster,
        respectExt = school.reRespectExt,
        name = langMapEntities.toIModel(),
        rpId = school.reRpId,
        schoolCode = school.reSchoolCode,
        lastModified = school.reLastModified,
        stored = school.reStored
    )
}
