package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.BookmarkEntity
import world.respect.datalayer.school.model.StatusEnum
import world.respect.datalayer.school.model.Bookmark


fun BookmarkEntity.toModel(): Bookmark {
    return Bookmark(
        learningUnitManifestUrl = bLearningUnitManifestUrl,
        title = bTitle,
        subtitle = bSubtitle,
        appIcon = bAppIcon,
        appName = bAppName,
        iconUrl = bIconUrl,
        appManifestUrl = bAppManifestUrl,
        expectedIdentifier = bExpectedIdentifier,
        refererUrl = bRefererUrl,
        personUid = bPersonUid
    )
}

fun Bookmark.toEntities(
    uidNumberMapper: UidNumberMapper
): BookmarkEntity {
    return BookmarkEntity(
        bUrlHash = uidNumberMapper(learningUnitManifestUrl),
        bPersonUid = personUid,
        bPersonUidNum = uidNumberMapper(personUid) ,
        bLearningUnitManifestUrl = learningUnitManifestUrl,
        bTitle = title,
        bSubtitle = subtitle,
        bAppIcon = appIcon,
        bAppName = appName,
        bIconUrl = iconUrl,
        bAppManifestUrl = appManifestUrl,
        bExpectedIdentifier = expectedIdentifier,
        bRefererUrl = refererUrl,
        bStatus = StatusEnum.ACTIVE.flag,


        bCreatedAt = System.currentTimeMillis(),
        bUpdatedAt = System.currentTimeMillis()
    )
}
