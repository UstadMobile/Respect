package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.db.school.entities.BookmarkEntity
import world.respect.datalayer.school.model.Bookmark

fun BookmarkEntity.toModel(): Bookmark {
    return Bookmark(
        status = bStatus,
        lastModified = bLastModified,
        stored = bStored,
        personUid = bPersonUid,
        learningUnitManifestUrl = bLearningUnitManifestUrl,
        title = bTitle,
        subtitle = bSubtitle,
        appIcon = bAppIcon,
        appName = bAppName,
        iconUrl = bIconUrl,
        appManifestUrl = bAppManifestUrl,
        expectedIdentifier = bExpectedIdentifier,
        refererUrl = bRefererUrl,
    )
}

fun Bookmark.toEntities(): BookmarkEntity {
    return BookmarkEntity(
        bStatus = status,
        bLastModified = lastModified,
        bStored = stored,
        bPersonUid = personUid,
        bLearningUnitManifestUrl = learningUnitManifestUrl,
        bTitle = title,
        bSubtitle = subtitle,
        bAppIcon = appIcon,
        bAppName = appName,
        bIconUrl = iconUrl,
        bAppManifestUrl = appManifestUrl,
        bExpectedIdentifier = expectedIdentifier,
        bRefererUrl = refererUrl,
    )
}