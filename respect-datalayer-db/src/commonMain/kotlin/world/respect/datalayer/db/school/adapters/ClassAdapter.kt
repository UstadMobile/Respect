package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.db.school.entities.ClassEntity
import world.respect.datalayer.school.model.Clazz
import world.respect.libxxhash.XXStringHasher

data class ClassEntities(
    val clazz: ClassEntity
)

fun ClassEntities.toModel(): Clazz {
    return Clazz(
        guid = clazz.cGuid,
        title = clazz.cTitle,
        status = clazz.cStatus,
        description = clazz.cDescription,
        lastModified = clazz.cLastModified,
        stored = clazz.cStored,
        teacherInviteCode = clazz.cTeacherInviteCode,
        studentInviteCode = clazz.cStudentInviteCode,
    )
}


fun Clazz.toEntities(
    hasher: XXStringHasher,
): ClassEntities {
    TODO()
}