package world.respect.datalayer.db.school.adapters


import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.BookmarkEntity
import world.respect.datalayer.school.model.Bookmark


fun BookmarkEntity.toModel(): Bookmark {

    return Bookmark(
        status = bStatus,
        lastModified = bLastModified,
        stored = bStored,
        personUid = bPersonUid,
        learningUnitManifestUrl = bLearningUnitManifestUrl,
    )
}

fun Bookmark.toEntities(uidNumberMapper: UidNumberMapper,): BookmarkEntity {
    return BookmarkEntity(
        bStatus = status,
        bLastModified = lastModified,
        bStored = stored,
        bPersonUid = personUid,
        bPersonUidHash = uidNumberMapper(personUid),
        bLearningUnitManifestUrl = learningUnitManifestUrl,
        bLearningUnitUrlHash = uidNumberMapper(learningUnitManifestUrl)
    )
}
