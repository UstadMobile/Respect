package world.respect.datalayer.db.bookmarks.adapters

import world.respect.datalayer.db.bookmarks.entities.BookmarkEntity
import world.respect.datalayer.school.model.StatusEnum
import world.respect.lib.opds.model.Bookmark


fun BookmarkEntity.toBookmark(): Bookmark {
    return Bookmark(

        learningUnitUrl = learningUnitUrl,
        title = title,
        subtitle = subtitle,
        appIcon = appIcon,
        appName = appName,
        iconUrl = iconUrl,
        appManifestUrl = appManifestUrl,
        expectedIdentifier = expectedIdentifier,
        refererUrl = refererUrl
    )
}

fun Bookmark.toBookmarkEntity(
    urlHash: Long,
): BookmarkEntity {
    return BookmarkEntity(
        urlHash = urlHash,
        learningUnitUrl = learningUnitUrl,
        title = title,
        subtitle = subtitle,
        appIcon = appIcon,
        appName = appName,
        iconUrl = iconUrl,
        appManifestUrl = appManifestUrl,
        expectedIdentifier = expectedIdentifier,
        refererUrl = refererUrl,
        status = StatusEnum.ACTIVE.flag,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
