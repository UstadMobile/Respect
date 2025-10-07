package world.respect.datalayer.db.schooldirectory.adapters

import androidx.room.Embedded
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntity
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.libxxhash.XXStringHasher

data class SchoolDirectoryEntities(
    @Embedded
    val directory: SchoolDirectoryEntity,
)

fun RespectSchoolDirectory.toEntities(
    xxStringHasher: XXStringHasher,
): SchoolDirectoryEntities {
    val rdUid = xxStringHasher.hash(baseUrl.toString())
    return SchoolDirectoryEntities(
        directory = SchoolDirectoryEntity(
            rdUid = rdUid,
            rdUrl = baseUrl,
            rdInvitePrefix = invitePrefix,
        )
    )
}

fun SchoolDirectoryEntities.toModel(): RespectSchoolDirectory {
    return RespectSchoolDirectory(
        invitePrefix = directory.rdInvitePrefix,
        baseUrl = directory.rdUrl,
    )
}
